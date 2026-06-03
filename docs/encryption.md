# Field Encryption

Field encryption is optional and disabled by default.

Use it for sensitive values stored in the database, such as:

- third-party API keys
- provider access tokens
- webhook secrets
- private integration credentials

Do not use it for passwords. Passwords must be hashed with BCrypt/Argon2, not encrypted.

## Configuration

```env
FIELD_ENCRYPTION_ENABLED=false
FIELD_ENCRYPTION_KEY=
```

`FIELD_ENCRYPTION_KEY` must be a base64-encoded 32-byte key.

Generate one with:

```bash
openssl rand -base64 32
```

## Format

Encrypted values are stored as:

```txt
enc:v1:<base64url-iv>:<base64url-ciphertext>
```

The `v1` marker leaves room for future key rotation or format changes.

## Algorithm

The starter uses:

```txt
AES-256-GCM
12-byte random IV
128-bit authentication tag
```

AES-GCM provides confidentiality and tamper detection.

## Usage

Inject `FieldEncryptionService` in services that manage sensitive values:

```kotlin
val encrypted = fieldEncryptionService.encrypt(secret)
val plain = fieldEncryptionService.decrypt(encrypted)
```

For JPA fields, extend `EncryptedStringConverterSupport` with a concrete converter and apply it only to fields that intentionally store secrets.

## Production Notes

- Store the key in a secret manager, not in source control.
- Rotate keys with a planned migration strategy.
- Do not encrypt fields you need to search/filter normally.
- Keep HTTPS enabled; field encryption protects stored data, not network traffic.
