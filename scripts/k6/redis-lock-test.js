import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import {Counter, Trend} from 'k6/metrics';

export let registered = new Counter('registered');
export let fullCount = new Counter('full');           // ← custom Counter
export let lockWait = new Trend('lock_wait_time');

export let options = {
    vus: 200,
    duration: '30s',
    thresholds: {
        registered: ['count>=100'],    // 등록된 건수 검증
        full: ['count>=100'],    // 가득참 건수 검증
        lock_wait_time: ['p(95)<500'],     // 락 대기 95백분위 <500ms
        http_req_duration: ['p(95)<600'],     // 응답 속도 95백분위 <600ms
    },
};

const tokens = new SharedArray('tokens', () =>
    parse(open('tokens.csv'), {header: true}).data
);

export function setup() {
    return {ts: Date.now() + 15_000, token: tokens[0].token};
}

export default function (data) {
    // 동시 진입 타이밍 맞추기
    while (Date.now() < data.ts) {
    }

    const start = Date.now();
    const res = http.post(
        `${__ENV.BASE_URL || 'http://localhost:8080'}/api/events/${__ENV.EVENT_ID || 1}/register`,
        null,
        {headers: {Authorization: `Bearer ${data.token}`}}
    );
    lockWait.add(Date.now() - start);

    // 체크하고, Counter 증가
    const isRegistered = res.status === 200;
    const isFull = res.status === 400 && res.json('code') === 'EVENT_402';
    const isBusy = res.status === 503 && res.json('code') === 'SYSTEM_BUSY';

    check(res, {
        'registered': () => isRegistered,
        'full': () => isFull,
        'system_busy': () => isBusy,
    });

    if (isRegistered) registered.add(1);
    if (isFull) fullCount.add(1);

    // sleep 제거 — 즉시 다음 iteration 진입
}