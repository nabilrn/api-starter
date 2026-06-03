# Database

The starter uses PostgreSQL with Flyway migrations. Hibernate schema generation is disabled with `ddl-auto: validate`; schema changes must be made through SQL migrations.

Migration files live in:

```txt
src/main/resources/db/migration
```

Flyway runs automatically on application startup:

```powershell
.\gradlew.bat bootRun
```

For the full command reference, see [Commands](commands.md).

## Tables

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`
- `oauth_accounts`

## Identity

All primary keys use UUIDs.

## User Accounts

Email uniqueness is enforced case-insensitively with a unique index on `lower(email)`.

Users can be:

- local auth users with `password_hash`
- OAuth-only users with no password hash
- linked users with both local credentials and OAuth accounts

## Tokens

Refresh tokens are stored by hash in `refresh_tokens.token_hash`. Raw refresh tokens must never be stored.

## OAuth

OAuth identities live in `oauth_accounts`, separate from `users`, so a user account can link provider identities without making the provider the primary account model.
