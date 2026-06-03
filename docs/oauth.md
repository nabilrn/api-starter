# OAuth

OAuth is optional and disabled by default.

The starter includes the reusable server-side account-linking core for Google and GitHub identities. It does not expose an insecure endpoint where a client can submit arbitrary provider profile data.

## Supported Providers

- Google
- GitHub

## Configuration

```env
OAUTH_GOOGLE_ENABLED=false
OAUTH_GOOGLE_CLIENT_ID=
OAUTH_GOOGLE_CLIENT_SECRET=

OAUTH_GITHUB_ENABLED=false
OAUTH_GITHUB_CLIENT_ID=
OAUTH_GITHUB_CLIENT_SECRET=
```

## Data Model

OAuth identities are stored in `oauth_accounts`.

This allows:

- local-only users
- OAuth-only users
- users with local credentials and linked OAuth accounts
- adding more providers later

## Service Boundary

`OAuthAccountService` expects a verified `OAuthProfile`.

A future provider callback/success handler should:

1. complete the OAuth provider flow
2. verify the provider identity
3. build an `OAuthProfile`
4. call `OAuthAccountService.authenticate(profile)` or `linkCurrentUser(userId, profile)`

## Behavior

Authentication:

- existing OAuth account: issue local JWT and refresh token pair
- new OAuth identity with existing local email: link OAuth account to that user
- new OAuth identity with new email: create OAuth-only user and assign `USER`

Linking:

- linking the same provider account to the same user is idempotent
- linking a provider account already owned by another user fails with conflict

OAuth-only users have no password hash. They can later be extended with a password setup flow if needed.
