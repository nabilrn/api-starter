# Users And Roles

The starter includes basic current-user and admin user management.

## Current User

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`

`PATCH /api/v1/users/me` currently supports updating the display name.

## Admin Users

Admin endpoints require `ROLE_ADMIN`.

- `GET /api/v1/admin/users`
- `GET /api/v1/admin/users/{userId}`
- `PATCH /api/v1/admin/users/{userId}/status`
- `PUT /api/v1/admin/users/{userId}/roles`

## Role Rules

Default roles are seeded by Flyway:

- `USER`
- `ADMIN`

Role assignment replaces the user's current role set. Requested role names are normalized to uppercase before lookup.
