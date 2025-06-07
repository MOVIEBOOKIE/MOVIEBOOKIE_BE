import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {parse} from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

// CSV 에서 토큰 로드 (optional)
const tokens = new SharedArray('tokens', () => {
    return parse(open('tokens.csv'), {header: true}).data;
});

export let options = {
    vus: __ENV.VUS ? parseInt(__ENV.VUS) : 100,
    duration: __ENV.DURATION || '1m',
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EVENT_ID = __ENV.EVENT_ID || '1';

export default function () {
    const token = tokens.length
        ? tokens[(__VU - 1) % tokens.length].token
        : __ENV.AUTH_TOKEN;

    let res = http.post(
        `${BASE_URL}/api/events/${EVENT_ID}/recruit`,
        {},
        {headers: {Authorization: `Bearer ${token}`}}
    );

    check(res, {
        'status is OK': (r) => r.status === 200 || r.status === 201,
    });

    sleep(0.1);
}
