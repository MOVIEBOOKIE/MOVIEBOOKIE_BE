import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {Counter, Rate, Trend} from 'k6/metrics';

const EVENT_ID = String(__ENV.EVENT_ID || '5');
const BASE_URLS = (__ENV.BASE_URLS || 'http://localhost:8080,http://localhost:8081,http://localhost:8082')
  .split(',')
  .map((url) => url.trim())
  .filter((url) => url.length > 0);
const LOCK_WAIT_MS = Number(__ENV.LOCK_WAIT_MS || 3000);
const LOCK_LEASE_MS = Number(__ENV.LOCK_LEASE_MS || 8000);
const TTL_WARN_MS = Number(__ENV.TTL_WARN_MS || Math.floor(LOCK_LEASE_MS * 0.9));
const TOKEN_FILE = __ENV.TOKEN_FILE || '../tokens.csv';
const RETRY_ON_SYSTEM_BUSY = (__ENV.RETRY_ON_SYSTEM_BUSY || 'true').toLowerCase() === 'true';
const RETRY_JITTER_MIN_MS = Number(__ENV.RETRY_JITTER_MIN_MS || 50);
const RETRY_JITTER_MAX_MS = Number(__ENV.RETRY_JITTER_MAX_MS || 150);

const tokens = new SharedArray('tokens', function () {
  return open(TOKEN_FILE)
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
});

if (tokens.length < 500) {
  throw new Error(`At least 500 tokens are required. current=${tokens.length}`);
}

if (BASE_URLS.length === 0) {
  throw new Error('BASE_URLS must contain at least one URL');
}

const registerSuccess = new Counter('register_success');
const registerFull = new Counter('register_full');
const registerAlready = new Counter('register_already_registered');
const registerSystemBusy = new Counter('register_system_busy');
const registerOtherFailure = new Counter('register_other_failure');
const registerCheckFailed = new Counter('register_check_failed');
const eventReadSuccess = new Counter('event_read_success_after_register');
const registerRetryAttempt = new Counter('register_retry_attempt');
const registerRetryRecovered = new Counter('register_retry_recovered');

const reqTarget8080 = new Counter('req_target_8080');
const reqTarget8081 = new Counter('req_target_8081');
const reqTarget8082 = new Counter('req_target_8082');
const reqTargetOther = new Counter('req_target_other');
const handledBy8080 = new Counter('handled_by_8080');
const handledBy8081 = new Counter('handled_by_8081');
const handledBy8082 = new Counter('handled_by_8082');
const handledByOther = new Counter('handled_by_other');

const ttlAnomalyRate = new Rate('ttl_anomaly_rate');
const ttlSuspectedTooLong = new Counter('ttl_suspected_too_long');
const ttlSuspectedTooShort = new Counter('ttl_suspected_too_short');
const registerDuration = new Trend('register_duration_ms', true);

export const options = {
  scenarios: {
    register_500_users: {
      executor: 'per-vu-iterations',
      vus: 500,
      iterations: 1,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.2'],
    http_req_duration: ['p(95)<1500', 'p(99)<3000'],
    register_duration_ms: ['p(95)<1500', 'p(99)<3000'],
    ttl_anomaly_rate: ['rate<0.1'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
  return {
    startTs: Date.now() + Number(__ENV.START_DELAY_MS || 3000),
  };
}

export default function (data) {
  while (Date.now() < data.startTs) {
    sleep(0.01);
  }

  const baseUrl = pickBaseUrl(__VU);
  const token = tokens[__VU - 1];
  markRequestTarget(baseUrl);

  let res = http.post(
    `${baseUrl}/api/events/${EVENT_ID}/register`,
    null,
    {
      headers: {Authorization: `Bearer ${token}`},
      responseCallback: http.expectedStatuses(200, 400, 401, 403, 429, 500),
      tags: {
        api: 'registerEvent',
        event_id: EVENT_ID,
        instance_target: extractPort(baseUrl),
      },
    }
  );

  let code = safeJsonCode(res);
  let isSystemBusy = (res.status === 503) || (res.status === 400 && code === 'SYSTEM_503');
  if (RETRY_ON_SYSTEM_BUSY && isSystemBusy) {
    registerRetryAttempt.add(1);
    sleep(randomJitterSeconds(RETRY_JITTER_MIN_MS, RETRY_JITTER_MAX_MS));
    res = http.post(
      `${baseUrl}/api/events/${EVENT_ID}/register`,
      null,
      {
        headers: {Authorization: `Bearer ${token}`},
        responseCallback: http.expectedStatuses(200, 400, 401, 403, 429, 500),
        tags: {
          api: 'registerEventRetry',
          event_id: EVENT_ID,
          instance_target: extractPort(baseUrl),
        },
      }
    );
    code = safeJsonCode(res);
    isSystemBusy = (res.status === 503) || (res.status === 400 && code === 'SYSTEM_503');
    if (!isSystemBusy) {
      registerRetryRecovered.add(1);
    }
  }

  registerDuration.add(res.timings.duration);
  markHandledInstance(res);

  const isSuccess = res.status === 200;
  const isFull = res.status === 400 && code === 'EVENT_402';
  const isAlready = res.status === 400 && code === 'USER_EVENT_401';

  const withinExpectedBusinessResult = isSuccess || isFull || isAlready || isSystemBusy;
  const valid = check(res, {
    'register: expected business result': () => withinExpectedBusinessResult,
    'register: no severe server error': () => res.status < 500 || isSystemBusy,
  });
  if (!valid) {
    registerCheckFailed.add(1);
  }

  if (isSuccess) {
    registerSuccess.add(1);
    verifyEventReadable(baseUrl, token);
  } else if (isFull) {
    registerFull.add(1);
  } else if (isAlready) {
    registerAlready.add(1);
  } else if (isSystemBusy) {
    registerSystemBusy.add(1);
  } else {
    registerOtherFailure.add(1);
  }

  const tooLong = res.timings.duration > TTL_WARN_MS;
  const tooShortBusy = isSystemBusy && res.timings.waiting < Math.max(50, LOCK_WAIT_MS * 0.2);
  const ttlAnomaly = tooLong || tooShortBusy;
  ttlAnomalyRate.add(ttlAnomaly);

  if (tooLong) {
    ttlSuspectedTooLong.add(1);
  }
  if (tooShortBusy) {
    ttlSuspectedTooShort.add(1);
  }
}

function verifyEventReadable(baseUrl, token) {
  const readRes = http.get(`${baseUrl}/api/events/${EVENT_ID}`, {
    headers: {Authorization: `Bearer ${token}`},
    responseCallback: http.expectedStatuses(200, 400, 401, 403),
    tags: {api: 'readEventAfterRegister', event_id: EVENT_ID},
  });

  if (readRes.status === 200) {
    eventReadSuccess.add(1);
  }
}

function safeJsonCode(res) {
  try {
    return res.json('code');
  } catch (e) {
    return '';
  }
}

function pickBaseUrl(vu) {
  return BASE_URLS[(vu - 1) % BASE_URLS.length];
}

function extractPort(url) {
  const matched = String(url).match(/:(\d+)/);
  return matched ? matched[1] : 'unknown';
}

function markRequestTarget(url) {
  if (url.includes(':8080')) {
    reqTarget8080.add(1);
    return;
  }
  if (url.includes(':8081')) {
    reqTarget8081.add(1);
    return;
  }
  if (url.includes(':8082')) {
    reqTarget8082.add(1);
    return;
  }
  reqTargetOther.add(1);
}

function markHandledInstance(res) {
  const header = firstHeaderValue(res, 'X-Instance-Id').toLowerCase();
  if (header.includes('8080')) {
    handledBy8080.add(1);
    return;
  }
  if (header.includes('8081')) {
    handledBy8081.add(1);
    return;
  }
  if (header.includes('8082')) {
    handledBy8082.add(1);
    return;
  }
  handledByOther.add(1);
}

function firstHeaderValue(res, name) {
  if (!res || !res.headers) {
    return '';
  }
  const raw = res.headers[name] || res.headers[name.toLowerCase()];
  if (Array.isArray(raw)) {
    return raw[0] || '';
  }
  return raw || '';
}

function randomJitterSeconds(minMs, maxMs) {
  const low = Math.min(minMs, maxMs);
  const high = Math.max(minMs, maxMs);
  const value = low + Math.random() * (high - low);
  return value / 1000;
}
