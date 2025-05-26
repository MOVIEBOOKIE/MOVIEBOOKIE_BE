# MOVIEBOOKIE_BE
--
🍿 영화관을 더 쉽고 더 자유롭게, 무비부키

## Project Structure
--
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

## 커밋 컨벤션 
--
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
