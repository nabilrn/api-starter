# Clone And Start A New Project

Use this flow when creating a real project from this starter.

## 1. Clone The Starter

```powershell
git clone <starter-repo-url> my-new-api
cd my-new-api
```

## 2. Remove Starter Git History

```powershell
Remove-Item -Recurse -Force .git
git init
```

## 3. Rename Project Metadata

Update:

```txt
settings.gradle.kts
README.md
AGENTS.md
docs/plan.md
```

In `settings.gradle.kts`:

```kotlin
rootProject.name = "my-new-api"
```

## 4. Rename Base Package

Current package:

```txt
com.apistarter
```

Example target package:

```txt
com.company.myapi
```

Rename:

```txt
src/main/kotlin/com/apistarter
src/test/kotlin/com/apistarter
```

Then update Kotlin package declarations and imports from:

```txt
com.apistarter
```

to your new package.

## 5. Configure Environment

For local development:

```powershell
Copy-Item .env.example .env
```

Minimum values to review:

```env
APP_NAME=my-new-api
APP_ENV=dev
APP_PORT=8080

DB_NAME=my_new_api
DB_USER=postgres
DB_PASSWORD=postgres

JWT_ACCESS_SECRET=replace-with-access-secret-minimum-32-chars
JWT_REFRESH_SECRET=replace-with-refresh-secret-minimum-32-chars
```

Important: Spring Boot reads environment variables, not `.env` files directly. Docker Compose reads `.env` automatically. For the app process, set environment variables in your terminal, IDE run config, CI, or deployment platform.

## 6. Update Docker Compose

Review `docker-compose.yml`.

Recommended changes:

```yaml
container_name: my-new-api-postgres
```

Make sure database names match your env values.

## 7. Start Database

```powershell
docker compose up -d
```

## 8. Run API Server

```powershell
.\gradlew.bat bootRun
```

Flyway migrations run automatically on app startup.

## 9. Open API Docs

Swagger:

```txt
http://localhost:8080/swagger-ui.html
```

Health:

```txt
http://localhost:8080/api/v1/health
```

## 10. Run Verification

```powershell
.\gradlew.bat test
.\gradlew.bat check
```

## 11. Update AI Coding-Agent Context

Update project-specific context in:

```txt
AGENTS.md
CLAUDE.md
.cursor/rules/api-starter.mdc
.github/copilot-instructions.md
.windsurfrules
.clinerules
```

At minimum, update:

- project name
- domain/business rules
- base package
- modules that should not be changed casually
- testing and security expectations

## 12. Initial Commit

```powershell
git add .
git commit -m "Initial project from API starter"
```

## Checklist

- `.git` from starter removed
- new git repo initialized
- project name updated
- package renamed
- env reviewed
- database starts
- server starts
- Swagger opens
- tests pass
- `check` passes
- agent instructions updated
- initial commit created
