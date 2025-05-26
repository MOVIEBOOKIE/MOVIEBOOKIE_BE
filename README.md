# MOVIEBOOKIE_BE
🍿 영화관을 더 쉽고 더 자유롭게, 무비부키

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
