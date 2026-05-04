import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import {Counter} from 'k6/metrics';

const registerSuccess = new Counter('register_success');
const registerFull = new Counter('register_full');
const registerAlready = new Counter('register_already_registered');
const registerDateConflict = new Counter('register_date_conflict');
const registerUnauthorized = new Counter('register_unauthorized');
const registerOther = new Counter('register_other');
const registerInvalidOperation = new Counter('register_invalid_operation');
const registerEventNotFound = new Counter('register_event_not_found');
const registerServerError = new Counter('register_server_error');
const registerByStatusCode = new Counter('register_by_status_code');
const handledBy8080 = new Counter('handled_by_8080');
const handledBy8081 = new Counter('handled_by_8081');
const handledByOther = new Counter('handled_by_other');

export const options = {
    scenarios: {
        race_apply: {
            executor: 'per-vu-iterations',
            vus: __ENV.VUS ? parseInt(__ENV.VUS, 10) : 40,
            iterations: __ENV.ITERATIONS ? parseInt(__ENV.ITERATIONS, 10) : 1,
            maxDuration: __ENV.MAX_DURATION || '30s',
        },
    },
    thresholds: {
        // 비즈니스 400(EVENT_402 등)은 expectedStatuses로 실패율에서 제외됨
        http_req_failed: ['rate<0.05'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BASE_URLS = (__ENV.BASE_URLS || '')
    .split(',')
    .map(v => v.trim())
    .filter(v => v.length > 0);
const EVENT_ID = __ENV.EVENT_ID || '1';
const DEBUG_OTHER = (__ENV.DEBUG_OTHER || 'false').toLowerCase() === 'true';
const DEBUG_OTHER_LIMIT = __ENV.DEBUG_OTHER_LIMIT ? parseInt(__ENV.DEBUG_OTHER_LIMIT, 10) : 20;
let debugOtherCount = 0;

const tokens = new SharedArray('tokens', () =>
    parse(open('tokens.csv'), {header: true}).data
);

export function setup() {
    return {targetTs: Date.now() + (__ENV.START_DELAY_MS ? parseInt(__ENV.START_DELAY_MS, 10) : 3000)};
}

export default function (data) {
    const margin = 20;
    while (Date.now() < data.targetTs - margin) {
        sleep(0.005);
    }

    const targetBaseUrl = pickBaseUrl();
    const token = tokens[(__VU - 1) % tokens.length].token;
    const res = http.post(
        `${targetBaseUrl}/api/events/${EVENT_ID}/register`,
        null,
        {
            headers: {Authorization: `Bearer ${token}`},
            responseCallback: http.expectedStatuses(200, 400),
        }
    );

    markHandledInstance(res);
    const code = safeJsonCode(res);
    const isSuccess = res.status === 200;
    const isFull = res.status === 400 && code === 'EVENT_402';
    const isAlready = res.status === 400 && code === 'USER_EVENT_401';
    const isDateConflict = res.status === 400 && code === 'PARTICIPATION_404';
    const isUnauthorized = res.status === 401 || res.status === 403;
    const isInvalidOperation = res.status === 400 && code === 'EVENT_403';
    const isEventNotFound = res.status === 400 && code === 'EVENT_401';
    const isServerError = res.status >= 500;

    registerByStatusCode.add(1, {
        status: String(res.status),
        code: code || 'NO_CODE',
    });

    check(res, {
        'event-register: expected business result': () =>
            isSuccess || isFull || isAlready || isDateConflict,
    });

    if (isSuccess) {
        registerSuccess.add(1);
        return;
    }
    if (isFull) {
        registerFull.add(1);
        return;
    }
    if (isAlready) {
        registerAlready.add(1);
        return;
    }
    if (isDateConflict) {
        registerDateConflict.add(1);
        return;
    }
    if (isUnauthorized) {
        registerUnauthorized.add(1);
        return;
    }
    if (isInvalidOperation) {
        registerInvalidOperation.add(1);
        return;
    }
    if (isEventNotFound) {
        registerEventNotFound.add(1);
        return;
    }
    if (isServerError) {
        registerServerError.add(1);
        if (DEBUG_OTHER && debugOtherCount < DEBUG_OTHER_LIMIT) {
            debugOtherCount += 1;
            console.warn(
                `[register_server_error #${debugOtherCount}] status=${res.status}, code=${code || 'NO_CODE'}, body=${truncateBody(res.body)}`
            );
        }
        return;
    }
    registerOther.add(1);

    if (DEBUG_OTHER && debugOtherCount < DEBUG_OTHER_LIMIT) {
        debugOtherCount += 1;
        console.warn(
            `[register_other #${debugOtherCount}] status=${res.status}, code=${code || 'NO_CODE'}, body=${truncateBody(res.body)}`
        );
    }
}

function pickBaseUrl() {
    if (BASE_URLS.length === 0) {
        return BASE_URL;
    }
    return BASE_URLS[(__VU - 1) % BASE_URLS.length];
}

function safeJsonCode(res) {
    try {
        return res.json('code');
    } catch (e) {
        return '';
    }
}

function truncateBody(body) {
    if (!body) {
        return '';
    }
    const s = String(body);
    if (s.length <= 300) {
        return s;
    }
    return `${s.slice(0, 300)}...`;
}

function markHandledInstance(res) {
    const raw = res.headers['X-Instance-Id'] || res.headers['x-instance-id'] || '';
    const value = Array.isArray(raw) ? (raw[0] || '') : raw;
    const lower = String(value).toLowerCase();
    if (lower.includes('8080')) {
        handledBy8080.add(1);
        return;
    }
    if (lower.includes('8081')) {
        handledBy8081.add(1);
        return;
    }
    handledByOther.add(1);
}
