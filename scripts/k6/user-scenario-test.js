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
    let homeRes = http.get('https://api.movie-bookie.shop/api/events/home', {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtMDIwMjAyQG5hdmVyLmNvbSIsImNhdGVnb3J5IjoicmVmcmVzaCIsImlhdCI6MTc1MDU4OTM3NCwiZXhwIjoxNzUxNzk4OTc0fQ.dgZkEKvI2irgmGr9c5GwaQF18brAAUMyEYfQUg3YoBXB-sX5JYqgcJ8341UANgmoajtoUUE-Hj9SRcmDIReOQQ'
        }
    });
    check(homeRes, {
        '홈화면 조회 성공': (r) => r.status === 200
    });

    // 2. 이벤트 신청
    let registerRes = http.post('https://api.movie-bookie.shop/api/events/191/register', null, {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtMDIwMjAyQG5hdmVyLmNvbSIsImNhdGVnb3J5IjoicmVmcmVzaCIsImlhdCI6MTc1MDU4OTM3NCwiZXhwIjoxNzUxNzk4OTc0fQ.dgZkEKvI2irgmGr9c5GwaQF18brAAUMyEYfQUg3YoBXB-sX5JYqgcJ8341UANgmoajtoUUE-Hj9SRcmDIReOQQ'
        }
    });
    check(registerRes, {
        '이벤트 신청 성공': (r) => r.status === 200
    });

    // 3. 신청한 이벤트 상세 조회
    let detailRes = http.get('https://api.movie-bookie.shop/api/events/191', {
        headers: {
            'accept': '*/*',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtMDIwMjAyQG5hdmVyLmNvbSIsImNhdGVnb3J5IjoicmVmcmVzaCIsImlhdCI6MTc1MDU4OTM3NCwiZXhwIjoxNzUxNzk4OTc0fQ.dgZkEKvI2irgmGr9c5GwaQF18brAAUMyEYfQUg3YoBXB-sX5JYqgcJ8341UANgmoajtoUUE-Hj9SRcmDIReOQQ'
        }
    });
    check(detailRes, {
        '이벤트 상세 조회 성공': (r) => r.status === 200
    });

}
