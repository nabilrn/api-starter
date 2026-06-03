# Testing And Coverage

Use the Gradle wrapper for all build and test commands. Do not rely on a globally installed Gradle.

For the full command reference, see [Commands](commands.md).

## Commands

```bash
./gradlew test
./gradlew jacocoTestReport
./gradlew check
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat jacocoTestReport
.\gradlew.bat check
```

## Current Test Layers

- Unit tests for response wrappers, pagination, token hashing, JWT signing, principals, services, and mappers.
- Controller method tests for auth and user controllers.
- Web MVC test for the public health endpoint with the real security chain.
- Security filter tests for bearer-token authentication.
- Error handler tests for standard API error responses.

## Coverage Gate

`check` runs JaCoCo coverage verification.

Current minimums:

- line coverage: 70%
- branch coverage: 60%

The coverage gate excludes structural boilerplate:

- application bootstrap classes
- config classes
- domain entities
- DTOs
- persistence base classes
- repositories

Behavior-heavy code remains in scope, including:

- services
- controllers
- security filters/services
- exception handlers
- response and pagination helpers

## Integration Tests

Testcontainers dependencies are installed for PostgreSQL integration tests. Add database-backed tests when behavior needs real persistence, Flyway migrations, or repository interaction.

Keep pure service behavior in unit tests and reserve Testcontainers for persistence/security flows that cannot be verified reliably with mocks.
