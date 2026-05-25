# Event Register Concurrency Tests

## 1) `register-event-concurrency-500.js`
- 목적: `registerEvent` 동시성 제어(분산락 + 비관적 락), TTL 이상 징후, 중복 신청/정원 초과, 멀티 인스턴스 분산을 한 번에 검증
- 사용자 수: 고정 500 VU (1인 1요청)
- 토큰: `scripts/k6/tokens.csv` 사용

### 실행
```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/event/register-event-concurrency-500.js \
  -e EVENT_ID=5 \
  -e BASE_URLS=http://localhost:8080,http://localhost:8081,http://localhost:8082 \
  -e TOKEN_FILE=../tokens.csv \
  -e LOCK_WAIT_MS=1500 \
  -e LOCK_LEASE_MS=8000 \
  -e TTL_WARN_MS=7200 \
  -e RETRY_ON_SYSTEM_BUSY=true \
  -e RETRY_JITTER_MIN_MS=50 \
  -e RETRY_JITTER_MAX_MS=150 \
  --summary-export=/Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/benchmark-register-event-summary.json
```

### 주요 지표
- Latency: `http_req_duration`, `register_duration_ms` (p95, p99)
- TPS: `http_reqs` (k6 기본 제공)
- 성공/실패 분류: `register_success`, `register_full`, `register_already_registered`, `register_system_busy`
- 재시도 지표: `register_retry_attempt`, `register_retry_recovered`
- TTL 이상 징후:
  - `ttl_suspected_too_long` (응답시간이 TTL 경고치 초과)
  - `ttl_suspected_too_short` (`SYSTEM_503`이 지나치게 짧은 대기시간으로 발생)
  - `ttl_anomaly_rate`
- 멀티 인스턴스 분산:
  - 요청 대상: `req_target_8080`, `req_target_8081`, `req_target_8082`
  - 실제 처리: `handled_by_8080`, `handled_by_8081`, `handled_by_8082` (`X-Instance-Id` 헤더 기준)

### 결과 해석 포인트
- 정원 제어: `register_success + register_full + register_already_registered (+ register_system_busy)` 조합이 비즈니스 기대치에 부합하는지 확인
- 중복 신청 제어: `register_already_registered`가 정상적으로 관측되는지 확인
- TTL 튜닝: `ttl_suspected_too_long`/`ttl_suspected_too_short` 비율로 lease/wait 조정 근거 확보

### 서버 튜닝 기본값(코드 반영)
- `app.lock.event-register.wait-ms`: `3000ms → 1500ms` (무한 대기 방지 + 과도한 SYSTEM_BUSY 완화 균형점)
- `app.lock.event-register.lease-ms`: `10000ms → 8000ms` (과도한 조기 만료 위험 완화 + 장기 점유 상한 유지)
