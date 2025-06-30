import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

export let options = {
    vus: __ENV.VUS ? parseInt(__ENV.VUS) : 200,   // 동시 VU 수
    duration: __ENV.DURATION || '30s',            // 총 실행 시간
    thresholds: {http_req_failed: ['rate<0.05']} // 실패율 5% 미만 허용
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EVENT_ID = __ENV.EVENT_ID || '1';

const tokens = new SharedArray('tokens', () =>
    parse(open('tokens.csv'), {header: true}).data
);

export function setup() {
    return {targetTs: Date.now() + 15_000};
}

export default function (data) {
    const margin = 20; // ms
    while (Date.now() < data.targetTs - margin) {
        sleep(0.005);
    }

    const token = tokens[(__VU - 1) % tokens.length].token;
    const res = http.post(
        `${BASE_URL}/api/events/${EVENT_ID}/register`,
        {},
        {headers: {Authorization: `Bearer ${token}`}}
    );

    check(res, {
        'registered': r => r.status === 200,
        'full': r => r.status === 400 && r.json('code') === 'EVENT_402',
    }) || console.error(`VU=${__VU} ✗ ${res.status} ${res.body}`);

    sleep(45);
}
