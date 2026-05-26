# Multi-Instance Tests

## 1) `multi-instance-quick-check.js`
- **시나리오**: 요청 대상 인스턴스(예: 8080/8081)와 실제 처리 인스턴스 헤더(`X-Instance-Id`)를 비교해 분산 처리 확인
- **실행 (event-register)**
```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/multi-instance/multi-instance-quick-check.js \
  -e TEST_CASE=event-register \
  -e BASE_URLS=http://localhost:8080,http://localhost:8081 \
  -e EVENT_ID=5 \
  -e TOKENS=token1,token2,token3
```

- **실행 (email-send-lock)**
```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/multi-instance/multi-instance-quick-check.js \
  -e TEST_CASE=email-send-lock \
  -e BASE_URL=http://localhost:8080 \
  -e EMAIL=multi-instance-k6@test.com
```
