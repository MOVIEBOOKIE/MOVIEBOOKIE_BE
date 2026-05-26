AGENTS Instructions (Hardened)
This document defines practical rules for OpenAI Codex/agents working in this repository.
Keep changes small, verifiable, and production-safe.

0) Purpose & Scope
- Goal: improve code quality, implement/refactor safely, and preserve stable CI/CD.
- Current stack (source of truth: `build.gradle`):
  - Java 21, Spring Boot 3.2.5, Gradle
  - Spring Web, Spring Data JPA, Spring Security/OAuth2, Validation
  - MySQL, Redis/Redisson, Kafka, Quartz/Batch
  - Liquibase, Actuator/Prometheus, Loki (logback appender)
- API response convention:
  - Prefer `CommonResponse<T>` (`global/apiPayload/response`)
  - Some auth flows use `BaseResponse<T>` (`global/apiPayload/common`)
  - Error format is `ErrorResponse` via `GlobalExceptionHandler`

1) Guardrails (Do / Don’t)
Do
- Add or update tests when changing behavior.
- Preserve controller → service → repository boundaries.
- Keep validation annotations (`@Valid`, `@NotNull`, `@NotBlank`) on request DTOs/endpoints.
- Keep API response envelope compatibility (`CommonResponse`, `BaseResponse`, `ErrorResponse`).
- For DB changes, include Liquibase changeSet and rollback whenever possible.

Don’t
- Do not log secrets, tokens, PII, or raw credential payloads.
- Do not commit real secret files/keys or expand secret exposure in Git history.
- Do not perform destructive schema changes (drop/type rewrite) without explicit approval.
- Do not mix unrelated large refactors into one PR.

2) Branch & PR Policy
- Branch naming: `codex/<feature-name>` (kebab-case English).
- Recommended base branch:
  - feature/fix: `develop`
  - production hotfix/release: follow maintainer instruction (`main` flow)
- PR description must include:
  - summary and rationale
  - affected areas (API/DB/infra)
  - test instructions and results
  - Liquibase migration/rollback notes (if DB touched)
  - performance evidence (k6/metrics) when performance-related

3) Repository Map (Current)
- Single-module Spring Boot app at repository root.
- Code:
  - `src/main/java/project/luckybooky/domain/...`
  - `src/main/java/project/luckybooky/global/...`
- Resources:
  - `src/main/resources/application-*.yml`
  - `src/main/resources/db/changelog/**`
  - `src/main/resources/logback-spring.xml`
- CI/CD:
  - `.github/workflows/cicd-dev.yml`
  - `.github/workflows/cicd-prod.yml`
- Load/perf assets:
  - `scripts/k6/README.md`
  - `scripts/k6/tokens.csv`

4) Agent Task Protocol
- Analyze: locate exact impacted layers and contracts first.
- Plan: break work into reviewable commits; include rollback for risky changes.
- Implement: minimal diff, preserve API/DTO compatibility unless explicitly requested.
- Test: run targeted tests first, then broader tests as needed.
- Verify: include command output summary in final report.

5) Coding & API Rules
- Layering: controller → service → repository.
- Transactions:
  - read paths: `@Transactional(readOnly = true)` 권장
  - write paths: explicit writable transaction
- Exception handling:
  - use existing error code/exception pattern in `global/apiPayload/error`
- Mapping:
  - use existing converter classes (MapStruct is not currently configured)
- Pagination/response shape:
  - keep existing DTO/response field compatibility

6) Performance & Query Rules
- Prevent N+1 on collection/detail aggregation queries.
- Prefer explicit fetch strategies or bulk 조회 + 매핑.
- Validate before/after with query count and latency impact when relevant.

7) Database & Liquibase
- Master changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Useful commands (project conventions):
  - status(dev): `./gradlew status -PrunList=devUpdate`
  - status(prod): `./gradlew status -PrunList=prodUpdate`
  - diff files are stored in `src/main/resources/db/changelog-backup`
- Rules:
  - one changeSet per logical feature
  - clear id/author
  - rollback block required when feasible
  - destructive migration requires explicit approval + staged rollout plan

8) CI/CD (GitHub Actions)
- Dev pipeline: PR/Push to `develop` (`cicd-dev.yml`)
- Prod pipeline: PR/Push to `main` (`cicd-prod.yml`)
- Common flow:
  - Gradle bootJar build
  - Liquibase status gate
  - Docker build/push to ECR
  - EC2 deploy via SSH
- If touching CI/CD, document why and expected operational impact.

9) Observability & Security
- Logging backend: logback (`logback-spring.xml`) + Loki appender.
- Metrics: Spring Actuator + Prometheus registry.
- Security baseline:
  - no secret values in code, logs, PR text, or screenshots
  - external calls should keep timeout/retry/error handling patterns

10) Local Development
- Run tests: `./gradlew test`
- Run application locally: `./gradlew bootRun`
- Docker local stack (if needed): `docker compose -f docker-compose.yml up`

11) PR Checklist Template
- [ ] Scope and rationale are clear.
- [ ] API/DTO compatibility reviewed.
- [ ] Validation/exception/response conventions preserved.
- [ ] Tests added or updated and passing.
- [ ] Liquibase migration + rollback documented (if DB touched).
- [ ] Security/logging review done (no secrets/PII).
- [ ] CI impact and rollback plan documented (if infra/workflow touched).

12) Decision Notes (Light ADR)
- Record major decisions as:
  - `YYYY-MM-DD | Decision | Reason | Impact`

Any change conflicting with these rules requires explicit approval in PR.
