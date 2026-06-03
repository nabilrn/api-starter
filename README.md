# API Starter

Reusable Kotlin + Spring Boot REST API starter with PostgreSQL, Flyway, JWT auth, tests, coverage, optional OAuth, HMAC request signing, AES-GCM field encryption, and AI coding-agent setup.

## What Is Included

- Kotlin + Spring Boot 3
- Gradle Kotlin DSL + Gradle wrapper
- PostgreSQL + Flyway migrations
- Spring Data JPA
- Spring Security
- Local auth with JWT access tokens and hashed refresh-token rotation
- User profile and admin user/role management
- Optional Google/GitHub OAuth account-linking core
- Optional HMAC request signing for webhook/internal API paths
- Optional AES-GCM field encryption service for stored secrets
- OpenAPI / Swagger
- JUnit 5, MockK, MockMvc, Testcontainers, JaCoCo
- AI coding-agent rules for Codex, Claude, Cursor, Copilot, Windsurf, and Cline-style workflows

## Start Here

If you are creating a new project from this starter:

1. Follow [Clone and start a new project](docs/clone-and-start.md).
2. Then use [Commands](docs/commands.md) for daily development commands.

If you only want to run this starter locally:

```powershell
Copy-Item .env.example .env
docker compose up -d
.\gradlew.bat bootRun
```

Swagger:

```txt
http://localhost:8080/swagger-ui.html
```

Health:

```txt
http://localhost:8080/api/v1/health
```

## Common Commands

```powershell
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat jacocoTestReport
docker compose down
```

For the complete command list, including migration notes, see [Commands](docs/commands.md).

## Documentation

- [Clone and start a new project](docs/clone-and-start.md)
- [Commands](docs/commands.md)
- [Implementation plan](docs/plan.md)
- [Database model](docs/database.md)
- [Auth](docs/auth.md)
- [Users and roles](docs/users.md)
- [Testing and coverage](docs/testing.md)
- [OAuth](docs/oauth.md)
- [HMAC request signing](docs/hmac.md)
- [Field encryption](docs/encryption.md)
- [AI coding-agent setup](docs/ai-agent-setup.md)

## Agent Context

`AGENTS.md` is the main source of truth for AI coding agents working in this repository. Update it after cloning the starter for a real project.
