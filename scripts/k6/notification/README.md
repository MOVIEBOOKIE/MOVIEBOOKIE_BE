# Notification Messaging Tests

## 1) `admin-notification-outbox-500.js`
- 목적: 관리자 전체 공지 1회 실행으로 `5000명 / batchSize=100 / totalBatches=50`이 전부 정상 완료되는지 검증
- 대상 API:
  - `POST /api/admin/notifications/bulk/users`
  - `POST /api/admin/notifications/outbox/replay?limit=...`

### 실행
```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/notification/admin-notification-outbox-500.js \
  -e ADMIN_TOKEN=<admin_access_token> \
  -e BASE_URLS=http://localhost:8080 \
  -e BATCH_SIZE=100 \
  -e BULK_VUS=1 \
  -e EXPECT_TOTAL_TARGET=5000 \
  -e EXPECT_TOTAL_BATCHES=50 \
  -e STRICT_BULK_ASSERT=true \
  -e REPLAY_ENABLED=true \
  -e REPLAY_VUS=3 \
  -e REPLAY_DURATION=40s \
  -e REPLAY_PAUSE_SEC=0.2 \
  -e REPLAY_LOOP_SLEEP_SEC=0.3 \
  -e REPLAY_LIMIT=1000 \
  --summary-export=/Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/benchmark-admin-notification-summary.json
```

### 측정 지표
- 지연/처리량: `http_req_duration`, `admin_bulk_duration_ms`, `http_reqs`(TPS)
- 기능 검증:
  - 유실 의심: `messaging_loss_suspected_rate`
  - 재처리: `outbox_replay_ok`, `outbox_replay_recovered_total`
  - 멱등성: `replay_idempotency_rate` (연속 replay 2회 호출 시 2nd <= 1st)
  - 배치 완료성: `bulk_strict_pass`, `bulk_strict_fail`
- outbox 처리량 관측:
  - `outbox_queued_total`, `outbox_processed_total`, `push_sent_total`, `push_skipped_total`

### 해석 포인트
- `messaging_loss_suspected_rate`가 0에 가까운지 확인
- `replay_idempotency_rate`가 1에 가까운지 확인
- `outbox_replay_recovered_total > 0`이면 실패 이벤트 재처리 경로가 실제로 동작한 것
- `bulk_strict_fail=0`이어야 아래 조건을 모두 만족:
  - `totalTargetCount=5000`
  - `effectiveBatchSize=100`
  - `totalBatches=50`
  - `processedCount=queuedCount=savedCount=5000`

### 분산 환경 확인 방법(중요)
- 관리자 `bulk/users` 호출은 한 인스턴스로 들어가도 정상입니다.
- 멀티 인스턴스 검증 대상은 **요청 라우팅**이 아니라 **백그라운드 메시징 처리 분산**입니다.
- 아래를 인스턴스별로 확인하세요:
  - outbox publish 성공/실패 카운트
  - kafka consumer 처리 건수
  - outbox `PENDING/FAILED/PUBLISHED` 변화량

주의:
- `bulk/users`는 배치 Job 기동 API라 고동시성(예: 500 VU) 직접 타격 대상이 아닙니다.
- 대량 부하는 outbox publisher/consumer(Kafka) 구간에서 별도 관측하는 것이 맞습니다.
