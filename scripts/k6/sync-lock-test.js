import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import {Counter, Trend} from 'k6/metrics';

export let options = {
    vus: 200,
    duration: '30s',
    thresholds: {
        registered: ['count>=100'],    // 최소 100건 등록
        full_count: ['count>=100'],    // 최소 100건 가득참
        lock_wait_time: ['p(95)<500'],     // 락 대기 p95<500ms
        http_req_duration: ['p(95)<600'],     // 처리 지연 p95<600ms
    },
};

export let registered = new Counter('registered');
export let full_count = new Counter('full_count');
export let lock_wait_time = new Trend('lock_wait_time');

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
    lock_wait_time.add(Date.now() - start);

    // 체크 및 카운터 집계
    const ok1 = check(res, {
        'registered': r => r.status === 200,
        'full': r => r.status === 400 && r.json('code') === 'EVENT_402',
    });

    if (res.status === 200) {
        registered.add(1);
    } else if (res.status === 400 && res.json('code') === 'EVENT_402') {
        full_count.add(1);
    }
}
