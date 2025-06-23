import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '40s', target: 100 },
        { duration: '10s', target: 200 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.01'],            // 실패율 1% 미만
        'http_req_duration': ['p(95)<300', 'avg<300'], // 95% 요청 250ms 이하, 평균 300ms 이하
        'checks': ['rate>0.99'],                     // 체크 성공률 99% 이상
    },
};

export default function () {
    // 1. 홈화면 조회
    let homeRes = http.get('http://host.docker.internal:8080/api/events/home', {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdDFAZXhhbXBsZS5jb20iLCJjYXRlZ29yeSI6ImFjY2VzcyIsImlhdCI6MTc1MDYwMzkzNywiZXhwIjoxNzUxMjA4NzM3fQ.Xq7EH3N5XJE15R-rOA9-bDFN1OoCJsjwDzzNT5iWnO4CX4wTQIgUIwDON3P-TphHgTzkzyaWPFCvRcAzOBSzYw'
        }
    });
    check(homeRes, {
        '홈화면 조회 성공': (r) => r.status === 200
    });

    // 2. 이벤트 신청
    let registerRes = http.post('http://host.docker.internal:8080/api/events/2/register', null, {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdDRAZXhhbXBsZS5jb20iLCJjYXRlZ29yeSI6ImFjY2VzcyIsImlhdCI6MTc1MDE3NDcwNSwiZXhwIjoxNzUwNzc5NTA1fQ.onlomPzLxBXglb7uFce1-EdidPGLg5XHgtLCdEn7gAAUPG1g3jUEaDD5pvBQUuc_-cBiJ_T4j2ZmeOa2dcBfBg'
        }
    });
    check(registerRes, {
        '이벤트 신청 성공': (r) => r.status === 200
    });

    // 3. 신청한 이벤트 상세 조회
    let detailRes = http.get('http://host.docker.internal:8080/api/events/2', {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdDRAZXhhbXBsZS5jb20iLCJjYXRlZ29yeSI6ImFjY2VzcyIsImlhdCI6MTc1MDE3NDcwNSwiZXhwIjoxNzUwNzc5NTA1fQ.onlomPzLxBXglb7uFce1-EdidPGLg5XHgtLCdEn7gAAUPG1g3jUEaDD5pvBQUuc_-cBiJ_T4j2ZmeOa2dcBfBg'
        }
    });
    check(detailRes, {
        '이벤트 상세 조회 성공': (r) => r.status === 200
    });

}
