# Auth

The starter uses local email/password authentication first. OAuth is planned as an optional account-linking module.

## Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

## Token Model

Access tokens are JWT bearer tokens signed with HS256.

Refresh tokens are opaque random tokens. Only the SHA-256 hash of a refresh token is stored in the database.

Refresh token rotation is enabled:

- `/refresh` validates the current refresh token
- the current refresh token is revoked
- a new access token and refresh token pair is issued

## Passwords

Passwords are hashed with BCrypt.

## Roles

The first migration seeds:

- `USER`
- `ADMIN`

New registered users receive the `USER` role.

## Security Defaults

Public endpoints:

- health
- Swagger/OpenAPI
- register
- login
- refresh
- logout

All other endpoints require authentication unless explicitly opened.
