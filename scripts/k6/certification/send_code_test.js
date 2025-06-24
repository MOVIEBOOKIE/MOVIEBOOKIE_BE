import http from 'k6/http';
import {check, sleep} from 'k6';

// 부하 테스트 옵션: 300명의 VU를 1분간 유지
export const options = {
    vus: 300,
    duration: '1m',
    // 필요 시 ramp-up/다운 단계로 바꿀 수 있습니다.
    // stages: [
    //   { duration: '30s', target: 300 }, // 30초 동안 0→300 VUs ramp-up
    //   { duration: '1m', target: 300 },  // 1분 유지
    //   { duration: '30s', target: 0 },   // 30초 ramp-down
    // ],
};

const BASE_URL = 'https://your.api.server';  // ← 실제 호스트로 수정
const SEND_ENDPOINT = '/api/email/send-code'; // ← 실제 엔드포인트 경로로 수정
const HEADERS = {'Content-Type': 'application/json'};

export default function () {
    // 각 VU와 iteration 조합으로 고유 이메일 생성
    const email = `user${__VU}_${__ITER}@example.com`;
    const payload = JSON.stringify({email});

    // 인증코드 발송 요청
    const res = http.post(`${BASE_URL}${SEND_ENDPOINT}`, payload, {headers: HEADERS});

    // 응답 코드 200 확인
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // 사용자당 평균 1~3초 대기
    sleep(Math.random() * 2 + 1);
}
