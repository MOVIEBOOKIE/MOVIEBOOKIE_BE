# K6 Test Suite

기능별로 테스트 스크립트를 분리했습니다.

- `certification/`: 이메일·인증번호 발송 락/중복 테스트
- `event/`: 이벤트 신청 동시성/경합 테스트
- `lock/`: 분산락·동기화·lease time 튜닝 테스트
- `multi-instance/`: 다중 인스턴스 라우팅/처리 분산 확인
- `notification/`: 관리자 알림(outbox/kafka/replay) 메시징 부하 테스트
- `user/`: 사용자 시나리오(홈→신청→상세) 부하 테스트

공통 토큰 CSV:

- `scripts/k6/tokens.csv`

기본 실행 형태:

```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/<folder>/<script>.js
```

권장 시나리오(500명 이벤트 신청):

```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/event/register-event-concurrency-500.js \
  -e EVENT_ID=<eventId> \
  -e BASE_URLS=http://localhost:8080,http://localhost:8081,http://localhost:8082 \
  -e TOKEN_FILE=../tokens.csv
```
