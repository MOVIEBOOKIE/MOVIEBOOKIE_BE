import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';

const BASE_URLS = (__ENV.BASE_URLS || 'http://localhost:8080')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const ADMIN_TOKEN = (__ENV.ADMIN_TOKEN || '').trim();
const BATCH_SIZE = Number(__ENV.BATCH_SIZE || 100);
const EXPECT_TOTAL_TARGET = Number(__ENV.EXPECT_TOTAL_TARGET || 5000);
const EXPECT_TOTAL_BATCHES = Number(__ENV.EXPECT_TOTAL_BATCHES || Math.ceil(EXPECT_TOTAL_TARGET / BATCH_SIZE));
const STRICT_BULK_ASSERT = (__ENV.STRICT_BULK_ASSERT || 'true').toLowerCase() === 'true';
const REPLAY_LIMIT = Number(__ENV.REPLAY_LIMIT || 1000);
const REPLAY_ENABLED = (__ENV.REPLAY_ENABLED || 'true').toLowerCase() === 'true';

if (!ADMIN_TOKEN) {
  throw new Error('ADMIN_TOKEN is required');
}
if (BASE_URLS.length === 0) {
  throw new Error('BASE_URLS must contain at least one URL');
}

const bulkOk = new Counter('admin_bulk_ok');
const bulkFailed = new Counter('admin_bulk_failed');
const replayOk = new Counter('outbox_replay_ok');
const replayFailed = new Counter('outbox_replay_failed');

const queuedTotal = new Counter('outbox_queued_total');
const processedTotal = new Counter('outbox_processed_total');
const pushSentTotal = new Counter('push_sent_total');
const pushSkippedTotal = new Counter('push_skipped_total');
const replayRecoveredTotal = new Counter('outbox_replay_recovered_total');
const bulkStrictPass = new Counter('bulk_strict_pass');
const bulkStrictFail = new Counter('bulk_strict_fail');

const lossSuspectedRate = new Rate('messaging_loss_suspected_rate');
const idempotencyReplayRate = new Rate('replay_idempotency_rate');
const bulkDuration = new Trend('admin_bulk_duration_ms', true);

export const options = {
  scenarios: {
    bulk_notify_launcher: {
      executor: 'per-vu-iterations',
      vus: Number(__ENV.BULK_VUS || 1),
      iterations: 1,
      maxDuration: '2m',
      exec: 'bulkNotifyScenario',
    },
    replay_probe: {
      executor: 'constant-vus',
      vus: Number(__ENV.REPLAY_VUS || 3),
      duration: __ENV.REPLAY_DURATION || '40s',
      startTime: __ENV.REPLAY_START || '15s',
      exec: 'replayProbeScenario',
      gracefulStop: '10s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2500', 'p(99)<4000'],
    http_req_failed: ['rate<0.2'],
    admin_bulk_duration_ms: ['p(95)<2500', 'p(99)<4000'],
    messaging_loss_suspected_rate: ['rate<0.05'],
    replay_idempotency_rate: ['rate>0.90'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max'],
};

export function bulkNotifyScenario() {
  const baseUrl = pickBaseUrl(__VU);

  const payload = {
    title: `k6-admin-bulk-${Date.now()}-vu${__VU}`,
    body: `k6 load test message - vu=${__VU}`,
    batchSize: BATCH_SIZE,
  };

  const res = http.post(
    `${baseUrl}/api/admin/notifications/bulk/users`,
    JSON.stringify(payload),
    {
      headers: {
        Authorization: `Bearer ${ADMIN_TOKEN}`,
        'Content-Type': 'application/json',
      },
      responseCallback: http.expectedStatuses(200, 400, 401, 403, 500),
      tags: {
        api: 'adminBulkNotify',
        instance_target: extractPort(baseUrl),
      },
    }
  );

  bulkDuration.add(res.timings.duration);

  const ok = res.status === 200;
  const code = safeJsonPath(res, 'code');
  const result = safeJsonPath(res, 'result');
  const expectedBusinessResult = ok
    || res.status === 401
    || res.status === 403
    || code === 'ADMIN_500';

  check(res, {
    'bulk notify: expected business result': () => expectedBusinessResult,
    'bulk notify: no client payload error': () => res.status !== 400,
  });

  if (!ok) {
    bulkFailed.add(1);
    return;
  }
  bulkOk.add(1);

  const queuedCount = toNumber(result && result.queuedCount);
  const savedCount = toNumber(result && result.savedCount);
  const processedCount = toNumber(result && result.processedCount);
  const pushSentCount = toNumber(result && result.pushSentCount);
  const pushSkippedCount = toNumber(result && result.pushSkippedCount);

  queuedTotal.add(queuedCount);
  processedTotal.add(processedCount);
  pushSentTotal.add(pushSentCount);
  pushSkippedTotal.add(pushSkippedCount);

  const noLossSuspected = queuedCount >= processedCount && savedCount === queuedCount;
  const accountingOk = processedCount >= (pushSentCount + pushSkippedCount);
  const totalTargetCount = toNumber(result && result.totalTargetCount);
  const effectiveBatchSize = toNumber(result && result.effectiveBatchSize);
  const totalBatches = toNumber(result && result.totalBatches);

  const strictTotalTargetOk = totalTargetCount === EXPECT_TOTAL_TARGET;
  const strictBatchSizeOk = effectiveBatchSize === BATCH_SIZE;
  const strictBatchCountOk = totalBatches === EXPECT_TOTAL_BATCHES;
  const strictProcessedOk = processedCount === EXPECT_TOTAL_TARGET;
  const strictQueuedOk = queuedCount === EXPECT_TOTAL_TARGET;
  const strictSavedOk = savedCount === EXPECT_TOTAL_TARGET;
  const strictOk = strictTotalTargetOk
    && strictBatchSizeOk
    && strictBatchCountOk
    && strictProcessedOk
    && strictQueuedOk
    && strictSavedOk;

  check(res, {
    'outbox consistency: queued >= processed': () => noLossSuspected,
    'outbox consistency: processed accounting': () => accountingOk,
    'bulk strict: target count matches': () => !STRICT_BULK_ASSERT || strictTotalTargetOk,
    'bulk strict: batch size matches': () => !STRICT_BULK_ASSERT || strictBatchSizeOk,
    'bulk strict: total batches matches': () => !STRICT_BULK_ASSERT || strictBatchCountOk,
    'bulk strict: processed=target': () => !STRICT_BULK_ASSERT || strictProcessedOk,
    'bulk strict: queued=target': () => !STRICT_BULK_ASSERT || strictQueuedOk,
    'bulk strict: saved=target': () => !STRICT_BULK_ASSERT || strictSavedOk,
  });
  lossSuspectedRate.add(!noLossSuspected);
  if (STRICT_BULK_ASSERT) {
    if (strictOk) {
      bulkStrictPass.add(1);
    } else {
      bulkStrictFail.add(1);
    }
  }
}

export function replayProbeScenario() {
  if (!REPLAY_ENABLED) {
    sleep(1);
    return;
  }

  const baseUrl = pickBaseUrl(__VU);

  const first = replayOnce(baseUrl);
  sleep(Number(__ENV.REPLAY_PAUSE_SEC || 0.2));
  const second = replayOnce(baseUrl);

  if (first.ok) {
    replayRecoveredTotal.add(first.count);
  }

  const idempotent = first.ok && second.ok && second.count <= first.count;
  if (first.ok && second.ok) {
    idempotencyReplayRate.add(idempotent);
  }
  sleep(Number(__ENV.REPLAY_LOOP_SLEEP_SEC || 0.3));
}

function replayOnce(baseUrl) {
  const res = http.post(
    `${baseUrl}/api/admin/notifications/outbox/replay?limit=${REPLAY_LIMIT}`,
    null,
    {
      headers: {
        Authorization: `Bearer ${ADMIN_TOKEN}`,
      },
      responseCallback: http.expectedStatuses(200, 401, 403, 500),
      tags: {
        api: 'adminOutboxReplay',
        instance_target: extractPort(baseUrl),
      },
    }
  );
  if (res.status === 200) {
    replayOk.add(1);
    return {ok: true, count: toNumber(safeJsonPath(res, 'result'))};
  }
  replayFailed.add(1);
  return {ok: false, count: 0};
}

function safeJsonPath(res, path) {
  try {
    return res.json(path);
  } catch (e) {
    return null;
  }
}

function toNumber(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function pickBaseUrl(vu) {
  return BASE_URLS[(vu - 1) % BASE_URLS.length];
}

function extractPort(url) {
  const matched = String(url).match(/:(\d+)/);
  return matched ? matched[1] : 'unknown';
}
