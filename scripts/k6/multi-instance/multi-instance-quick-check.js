import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter} from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BASE_URLS = (__ENV.BASE_URLS || '')
  .split(',')
  .map((v) => v.trim())
  .filter((v) => v.length > 0);
const EVENT_ID = __ENV.EVENT_ID || '5';
const TEST_CASE = (__ENV.TEST_CASE || 'event-register').trim();
const TOKENS = (__ENV.TOKENS || '')
.split(',')
.map((v) => v.trim())
.filter((v) => v.length > 0);
const EMAIL = __ENV.EMAIL || 'multi-instance-k6@test.com';

const registerSuccess = new Counter('register_success');
const registerFull = new Counter('register_full');
const registerAlready = new Counter('register_already_registered');
const registerDateConflict = new Counter('register_date_conflict');
const registerUnauthorized = new Counter('register_unauthorized');
const registerOther = new Counter('register_other');
const certDuplicated = new Counter('cert_duplicated');
const certAccepted = new Counter('cert_accepted');
const reqTo8080 = new Counter('req_target_8080');
const reqTo8081 = new Counter('req_target_8081');
const reqToOther = new Counter('req_target_other');
const handledBy8080 = new Counter('handled_by_8080');
const handledBy8081 = new Counter('handled_by_8081');
const handledByOther = new Counter('handled_by_other');

export const options = {
  scenarios: {
    quick_check: {
      executor: 'per-vu-iterations',
      vus: Number(__ENV.VUS || 40),
      iterations: 1,
      maxDuration: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.2'],
  },
};

export function setup() {
  if (TEST_CASE === 'event-register' && TOKENS.length === 0) {
    throw new Error(
        'TEST_CASE=event-register requires TOKENS env (comma-separated bearer tokens).');
  }

  return {
    startTs: Date.now() + Number(__ENV.START_DELAY_MS || 3000),
  };
}

export default function (data) {
  while (Date.now() < data.startTs) {
    sleep(0.01);
  }

  if (TEST_CASE === 'event-register') {
    const token = TOKENS[(__VU - 1) % TOKENS.length];
    const targetBaseUrl = pickBaseUrl();
    markRequestTarget(targetBaseUrl);
    const res = http.post(
        `${targetBaseUrl}/api/events/${EVENT_ID}/register`,
        null,
        {
          headers: {Authorization: `Bearer ${token}`},
          responseCallback: http.expectedStatuses(200, 400),
        }
    );
    markHandledInstance(res);

    const isSuccess = res.status === 200;
    const code = safeJsonCode(res);
    const isFull = res.status === 400 && code === 'EVENT_402';
    const isAlready = res.status === 400 && code === 'USER_EVENT_401';
    const isDateConflict = res.status === 400 && code === 'PARTICIPATION_404';
    const isUnauthorized = res.status === 401 || res.status === 403;

    check(res, {
      'event-register: expected business result': () =>
          isSuccess || isFull || isAlready || isDateConflict,
    });

    if (isSuccess) {
      registerSuccess.add(1);
    } else if (isFull) {
      registerFull.add(1);
    } else if (isAlready) {
      registerAlready.add(1);
    } else if (isDateConflict) {
      registerDateConflict.add(1);
    } else if (isUnauthorized) {
      registerUnauthorized.add(1);
    } else {
      registerOther.add(1);
    }
    return;
  }

  if (TEST_CASE === 'email-send-lock') {
    const res = http.post(
        `${BASE_URL}/api/email/send`,
        JSON.stringify({email: EMAIL}),
        {headers: {'Content-Type': 'application/json'}}
    );

    const code = safeJsonCode(res);
    const isDuplicated = code === 'CERTIFICATION_DUPLICATED';
    const isAccepted = res.status === 200;

    check(res, {
      'email-send-lock: accepted or duplicated': () => isAccepted
          || isDuplicated,
    });

    if (isDuplicated) {
      certDuplicated.add(1);
    } else if (isAccepted) {
      certAccepted.add(1);
    }
    return;
  }

  throw new Error(`Unsupported TEST_CASE: ${TEST_CASE}`);
}

function safeJsonCode(res) {
  try {
    return res.json('code');
  } catch (e) {
    return '';
  }
}

function pickBaseUrl() {
  if (BASE_URLS.length === 0) {
    return BASE_URL;
  }
  return BASE_URLS[(__VU - 1) % BASE_URLS.length];
}

function markRequestTarget(url) {
  if (url.includes(':8080')) {
    reqTo8080.add(1);
    return;
  }
  if (url.includes(':8081')) {
    reqTo8081.add(1);
    return;
  }
  reqToOther.add(1);
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
