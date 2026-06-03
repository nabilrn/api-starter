# Commands

Use the Gradle wrapper. Do not rely on a globally installed Gradle.

## Windows PowerShell

### Start Database

```powershell
docker compose up -d
```

### Stop Database

```powershell
docker compose down
```

### Run API Server

```powershell
.\gradlew.bat bootRun
```

The API runs on:

```txt
http://localhost:8080
```

Swagger:

```txt
http://localhost:8080/swagger-ui.html
```

Health:

```txt
http://localhost:8080/api/v1/health
```

### Run Tests

```powershell
.\gradlew.bat test
```

### Generate Coverage Report

```powershell
.\gradlew.bat jacocoTestReport
```

Coverage report:

```txt
build/reports/jacoco/test/html/index.html
```

### Run Full Check

```powershell
.\gradlew.bat check
```

This runs tests and JaCoCo coverage verification.

## macOS / Linux

```bash
docker compose up -d
./gradlew bootRun
./gradlew test
./gradlew jacocoTestReport
./gradlew check
docker compose down
```

## Database Migrations

Migrations are managed by Flyway.

Migration files live in:

```txt
src/main/resources/db/migration
```

Current migrations:

```txt
V1__create_users_roles_refresh_tokens.sql
V2__create_oauth_accounts.sql
```

### How Migrations Run

Flyway runs automatically when the Spring Boot app starts:

```powershell
.\gradlew.bat bootRun
```

So the normal local flow is:

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

Spring Boot will:

1. connect to PostgreSQL
2. run pending Flyway migrations
3. validate JPA entities against the migrated schema
4. start the API server

## Adding A New Migration

Create a new SQL file:

```txt
src/main/resources/db/migration/V3__your_change_name.sql
```

Rules:

- increment the version number
- use lowercase table/column names
- keep migrations immutable after they are shared
- do not rely on Hibernate `ddl-auto` to modify schema

Then run:

```powershell
.\gradlew.bat bootRun
```

or:

```powershell
.\gradlew.bat check
```

`check` compiles and tests the project, but database migrations are applied when the application context starts with a database-backed test or when `bootRun` starts the app.
