# API Starter

Reusable Kotlin + Spring Boot API starter for REST backends with PostgreSQL, Flyway, JWT auth, tests, coverage, optional OAuth, HMAC, field encryption, and AI coding-agent setup.

## Stack

- Kotlin
- Spring Boot 3
- Gradle Kotlin DSL
- PostgreSQL
- Spring Data JPA
- Flyway
- Spring Security
- OpenAPI / Swagger
- JUnit 5, MockK, MockMvc, Testcontainers
- JaCoCo

## Local Setup

1. Copy `.env.example` to `.env`.
2. Start PostgreSQL:

```bash
docker compose up -d
```

3. Run the API:

```bash
./gradlew bootRun
```

4. Open Swagger:

```txt
http://localhost:8080/swagger-ui.html
```

See [Commands](docs/commands.md) for server, database, migration, test, and coverage commands.

## Test And Coverage

```bash
./gradlew test
./gradlew jacocoTestReport
./gradlew check
```

Coverage thresholds start at 70% line coverage and 60% branch coverage.
Use `gradlew.bat` on Windows PowerShell.

## Module Roadmap

1. Base foundation and agent instructions
2. Common response, errors, validation, pagination, health, and OpenAPI setup
3. PostgreSQL schema with Flyway
4. Local auth with JWT and refresh tokens
5. User and role management
6. Unit and integration tests
7. Optional OAuth
8. Optional HMAC request signing
9. Optional AES-GCM field encryption
10. AI coding-agent setup

## Implemented Foundation

- Standard `ApiResponse<T>` wrapper.
- Global exception handler with validation error formatting.
- `PageResponse<T>` pagination helper.
- OpenAPI metadata and JWT bearer security scheme.
- Stateless Spring Security baseline.
- Public `/api/v1/health` endpoint.
- Flyway schema for users, roles, refresh tokens, and OAuth accounts.
- JPA entities and repositories for the initial auth/user model.
- Local auth endpoints with JWT access tokens and hashed refresh-token rotation.
- Current-user profile endpoints and admin user/role management.
- Gradle wrapper, unit/web/security tests, and passing JaCoCo coverage gate.
- Optional OAuth account-linking core for Google/GitHub identities.
- Optional HMAC request signing for webhook/internal API paths.
- Optional AES-GCM field encryption service for stored secrets.
- AI coding-agent setup for Codex/Claude/Cursor/Copilot-style workflows.

## Docs

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
