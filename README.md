# MOVIEBOOKIE_BE
🍿 영화관을 더 쉽고 더 자유롭게, 무비부키

## System Architecture
<img width="800" alt="스크린샷 2025-06-20 오전 9 39 45" src="https://github.com/user-attachments/assets/b86f0c6a-a1be-4fc0-b70e-d0f21275ba93" />

## ERD
<img width="800" alt="무비부키 ERD" src="https://github.com/user-attachments/assets/e228266f-7930-4360-bc59-88e01835dc13" />

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
    ├── application-local.yml
    ├── application-dev.yml 
    └── application-prod.yml
```
## Tech Stack
- `Jdk 21`
- `Spring Boot 3.2.5`
- `MySQL 9.3.0`, `Redis 5.0.7`
- `NCP Server`, `NCP Object Storage`, `Docker`, `Github Action`
- `JPA`, `Swagger`, `Spring Security`, `JWT`, `FCM`



## Commit Convention
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

