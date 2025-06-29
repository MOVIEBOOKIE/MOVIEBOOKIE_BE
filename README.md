# MOVIEBOOKIE_BE
🍿 영화관을 더 쉽고 더 자유롭게, 무비부키

## ERD
<img width="800" alt="무비부키 ERD" src="https://github.com/user-attachments/assets/75c7d96b-4853-4284-9c5c-d77272540763" />

## System Architecture
<img width="800" alt="스크린샷 2025-06-20 오전 9 39 45" src="https://github.com/user-attachments/assets/b86f0c6a-a1be-4fc0-b70e-d0f21275ba93" />

## API Docs
**노션 링크**: https://waiting-candle-f33.notion.site/API-1cc3e5c872e78094864ccd251d8ea004?pvs=4

## Project Structure
```markdown
src
├── main
│   ├── domain
│   |    ├── category
│   |    ├── certification
│   |    ├── event
│   |    ├── feedback
│   |    ├── location
│   |    ├── notification
│   |    ├── participation
│   |    ├── user
│   |    |    ├── controller
│   |    |    ├── converter
│   |    |    ├── dto
│   |    |    |     ├── request
│   |    |    |     └── response
│   |    |    ├── entity
│   |    |    ├── repository
│   |    |    ├── service
│   |    |    └── util
│   |    └── ticket
│   |    
│   └── global
│        ├── apiPayload
│        ├── config
│        ├── entity
│        ├── handler
│        ├── jwt
│        ├── oauth
│        ├── redis
│        ├── service
│        └── util
│       
└── resources
    ├── application-dev.yml
    └── application-prod.yml
```
## Tech Stack
- `Jdk 21`
- `Spring Boot 3.2.5`
- `MySQL 9.3.0`, `Redis 5.0.7`
- `NCP Server`, `NCP Object Storage`, `Docker`, `Github Action`
- `JPA`, `Swagger`, `Spring Security`, `JWT`, `FCM`



### Commit Convention
| 커밋 타입 | 설명 | 예시 |
| ------- | ---- | ---- |
| ✨ **Feat** | 새로운 기능 추가 | `[FEAT] #이슈번호: 기능 추가` |
| 🐛 **Fix** | 버그 수정 | `[FIX] #이슈번호: 오류 수정` |
| 📄 **Docs** | 문서 수정 | `[DOCS] #이슈번호: README 파일 수정` |
| ♻️ **Refactor** | 코드 리팩토링 | `[REFACTOR] #이슈번호: 함수 구조 개선` |
| 📦 **Chore** | 빌드 업무 수정, 패키지 매니저 수정 등 production code와 무관한 변경 | `[CHORE] #이슈번호: .gitignore 파일 수정` |
| 💬 **Comment** | 주석 추가 및 변경 | `[COMMENT] #이슈번호: 함수 설명 주석 추가` |
| 🔥 **Remove** | 파일 또는 폴더 삭제 | `[REMOVE] #이슈번호: 불필요한 파일 삭제` |
| 🚚 **Rename** | 파일 또는 폴더명 수정 | `[RENAME] #이슈번호: 폴더명 변경` |


### Issue Template
```markdown
## Description
설명 작성

## To - Do
1.
2.
3.

## ETC
```

### PR Template
```markdown
## Issue

- 이슈 번호 및 링크


## Summary

- 요약 

## Describe your code

- 코드 설명 (설명이 필요한 코드가 있다고 생각하시면 간단하게 작성해주세요.)

# Check
- [ ] Reviewers 등록을 하였나요?
- [ ] Assignees 등록을 하였나요?
- [ ] 라벨 등록을 하였나요?
- [ ] PR 머지하기 전 반드시 CI가 정상적으로 작동하는지 확인해주세요!
```
