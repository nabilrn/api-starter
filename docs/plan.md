# API Starter Implementation Plan

## Goal

Create a reusable Kotlin + Spring Boot REST API starter for long-lived backend projects.

## Core Stack

- Kotlin
- Spring Boot 3
- Gradle Kotlin DSL
- PostgreSQL
- Spring Data JPA
- Flyway
- Spring Security
- JWT access tokens and hashed refresh tokens
- JUnit 5, MockK, MockMvc, Testcontainers
- JaCoCo coverage

## Optional Modules

- Google and GitHub OAuth account linking
- HMAC request signing for webhooks and partner/internal APIs
- AES-GCM field encryption for sensitive stored values
- AI coding-agent setup for clone-ready vibecoding workflows

## Build Order

1. Project scaffold, environment files, Docker Compose, and agent instructions. Done.
2. Common response, error handling, validation, pagination, health, and OpenAPI setup. Done.
3. PostgreSQL schema with Flyway migrations. Done.
4. Local auth with JWT access tokens and refresh token rotation. Done.
5. User and role management. Done.
6. Unit, web, security, and integration test coverage. Done.
7. Optional OAuth module. Done.
8. Optional HMAC request signing module. Done.
9. Optional AES-GCM field encryption module. Done.
10. AI coding-agent setup. Done.

## Architecture Rules

- REST is the default API interface.
- Keep gRPC out of the core starter unless internal service contracts require it later.
- Keep modules feature-based.
- Use Flyway for every schema change.
- Keep optional features disabled by default.
- Do not commit secrets or real environment files.
