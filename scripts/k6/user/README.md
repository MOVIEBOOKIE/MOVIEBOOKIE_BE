# User Scenario Tests

## 1) `user-scenario-test.js`
- **시나리오**: 홈 조회 → 이벤트 신청 → 이벤트 상세 조회의 사용자 플로우 부하 테스트
- **실행**
```bash
k6 run /Users/gwanghyeon/gwanghyeon/project/MOVIEBOOKIE_BE/scripts/k6/user/user-scenario-test.js
```

주의:
- 현재 스크립트 내부에 API URL/토큰이 하드코딩되어 있어, 실행 전 운영/개발 환경 값 확인이 필요합니다.
