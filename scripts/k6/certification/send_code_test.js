import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter} from 'k6/metrics';

export const options = {
    vus: 200,
    duration: '1m',
    thresholds: {
        'http_req_failed{status:>=500}': ['rate<0.01'],
        'cert_dup_count': ['count>=1'],
    },
};

let certDupCount = new Counter('cert_dup_count');
let dupSuccessCount = new Counter('cert_dup_success');

const BASE = 'http://localhost:8080';
const SEND = '/api/email/send';
const HEADERS = {'Content-Type': 'application/json'};
const BOUNDARY_DELAYS = [0.1, 1, 9]; // 0.1초, 1초, 9초

export default function () {
    // 1) 동시성 테스트: 매번 다른 이메일
    let email1 = `user${Math.floor(Math.random() * 100000)}@test.com`;
    let res1 = http.post(
        `${BASE}${SEND}`,
        JSON.stringify({email: email1}),
        {headers: HEADERS}
    );
    check(res1, {'status < 500': r => r.status < 500});

    // 2) 중복 요청 방지 boundary 테스트
    const dupEmail = 'duplicate@test.com';
    http.post(
        `${BASE}${SEND}`,
        JSON.stringify({email: dupEmail}),
        {headers: HEADERS}
    );

    // 0.1초, 1초, 9초 중 랜덤 지연
    sleep(BOUNDARY_DELAYS[Math.floor(Math.random() * BOUNDARY_DELAYS.length)]);

    let res2 = http.post(
        `${BASE}${SEND}`,
        JSON.stringify({email: dupEmail}),
        {headers: HEADERS}
    );
    if (res2.status === 400 || res2.status === 429) {
        certDupCount.add(1);
    } else if (res2.status < 500) {
        dupSuccessCount.add(1);
    }

    sleep(Math.random() * 2 + 1);
}
