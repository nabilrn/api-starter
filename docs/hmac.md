# HMAC Request Signing

HMAC request signing is optional and disabled by default.

It is intended for endpoints called by trusted systems instead of logged-in users, such as:

- webhooks
- partner APIs
- internal service callbacks

## Configuration

```env
HMAC_ENABLED=false
HMAC_SECRET=
HMAC_MAX_SKEW_SECONDS=300
HMAC_PROTECTED_PATHS=/api/v1/webhooks,/api/v1/internal
```

Only requests whose path starts with one of `HMAC_PROTECTED_PATHS` are verified.

## Headers

Signed requests must include:

```http
X-Timestamp: 2026-06-03T10:00:00Z
X-Nonce: unique-random-value
X-Signature: hmac-sha256=<base64url-signature>
```

## Canonical String

The signature is calculated over:

```txt
METHOD
path?query
timestamp
nonce
raw_body
```

Example:

```txt
POST
/api/v1/webhooks/payment?order=123
2026-06-03T10:00:00Z
nonce-123
{"status":"PAID"}
```

## Replay Protection

The verifier checks:

- timestamp is within the allowed skew window
- nonce has not been used before
- signature matches the request

The starter uses an in-memory nonce store. For multi-instance production deployments, replace it with Redis or another shared store.

## Important

HMAC does not encrypt data. It proves request authenticity and integrity. Use HTTPS in production, and use field encryption separately for sensitive data stored in the database.
