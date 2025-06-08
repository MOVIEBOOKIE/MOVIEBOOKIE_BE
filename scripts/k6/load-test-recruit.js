import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

// tokens.csv가 있다면 로드
const tokens = new SharedArray('tokens', () => {
    return parse(open('tokens.csv'), {header: true}).data;
});

export let options = {
    vus: __ENV.VUS ? parseInt(__ENV.VUS) : 200,
    duration: __ENV.DURATION || '1m',
    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EVENT_ID = __ENV.EVENT_ID || '1';

export default function () {
    // VU별 토큰 할당 (없으면 ENV Fallback)
    const token = tokens.length
        ? tokens[(__VU - 1) % tokens.length].token
        : __ENV.AUTH_TOKEN;

    const url = `${BASE_URL}/api/events/${EVENT_ID}/register`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
        },
    };

    // 요청 보내기
    let res = http.post(url, {}, params);

    // 검증 & 실패 로그
    let ok = check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'response code COMMON_200': (r) => r.json('code') === 'COMMON_200',
    });

    if (!ok) {
        console.error(`VU=${__VU} ▶︎ 요청 실패:
    URL: ${url}
    status: ${res.status}
    body: ${res.body.substring(0, 200)}...`);
    }

    sleep(0.1);
}
