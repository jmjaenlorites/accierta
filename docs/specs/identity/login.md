# Login

## Context
Identity bounded context. Login is how verified users authenticate to access the application.

## Description
A user logs in with email and password. The system validates credentials and returns a JWT access token and a refresh token. The access token is short-lived; the refresh token allows obtaining new access tokens without re-entering credentials.

## Domain model

### Entities
- **User** — existing aggregate from registration. Login reads the user's state but does not change it (no domain event emitted for a successful login — login is a query operation on credentials, not a state change).

### Value objects
- **AccessToken** — short-lived JWT containing userId, email, and expiration. Signed server-side.
- **RefreshToken** — long-lived opaque token stored server-side. Allows silent re-authentication.

### Domain events
- `LoginFailed` — emitted when a login attempt fails. Carries: email, reason, occurredAt. Useful for security monitoring / rate limiting.

## Rules and invariants

1. Only verified users can log in.
2. Password must match the stored bcrypt hash.
3. Access tokens expire after 15 minutes.
4. Refresh tokens expire after 7 days.
5. A refresh token can only be used once (rotation). Using a refresh token issues a new access + refresh pair and invalidates the old refresh token.
6. Using an already-invalidated refresh token invalidates ALL refresh tokens for that user (token theft detection).

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| RefreshSession | refreshToken | accessToken + new refreshToken | 5, 6 |
| Logout | refreshToken | void | Invalidates the refresh token |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| Authenticate | email, password | accessToken + refreshToken | Validates credentials, returns tokens |

## Behavior

### Happy path
1. User submits email + password.
2. System finds user by email, verifies the password hash.
3. System returns an access token (JWT, 15 min) and a refresh token (7 days).
4. Client includes the access token in subsequent requests via `Authorization: Bearer <token>`.
5. When the access token expires, the client sends the refresh token to get a new pair.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Wrong password | Password doesn't match hash | Reject with "invalid credentials" (don't reveal which field is wrong) |
| Unverified user | Email exists but not verified | Reject with "email not verified" |
| Unknown email | Email not registered | Reject with "invalid credentials" (same message as wrong password) |
| Expired refresh token | Refresh token older than 7 days | Reject, user must log in again |
| Reused refresh token | Already-used refresh token submitted | Invalidate ALL user's refresh tokens (potential theft) |
| Logout | User explicitly logs out | Invalidate the refresh token |

## Acceptance criteria

1. GIVEN a verified user with correct credentials WHEN Authenticate is executed THEN an access token and refresh token are returned.
2. GIVEN a wrong password WHEN Authenticate is executed THEN it is rejected with "invalid credentials".
3. GIVEN an unknown email WHEN Authenticate is executed THEN it is rejected with "invalid credentials" (same error as wrong password).
4. GIVEN an unverified user WHEN Authenticate is executed THEN it is rejected with "email not verified".
5. GIVEN a valid refresh token WHEN RefreshSession is executed THEN a new access token and new refresh token are returned, and the old refresh token is invalidated.
6. GIVEN an already-used refresh token WHEN RefreshSession is executed THEN ALL refresh tokens for that user are invalidated.
7. GIVEN a valid refresh token WHEN Logout is executed THEN the refresh token is invalidated.

## API contract

### POST /api/auth/login
Request:
```json
{
  "email": "user@example.com",
  "password": "securepassword"
}
```
Response (200):
```json
{
  "accessToken": "jwt-token",
  "refreshToken": "opaque-token",
  "expiresIn": 900
}
```
Errors: 401 (invalid credentials), 403 (email not verified)

### POST /api/auth/refresh
Request:
```json
{
  "refreshToken": "opaque-token"
}
```
Response (200):
```json
{
  "accessToken": "jwt-token",
  "refreshToken": "new-opaque-token",
  "expiresIn": 900
}
```
Errors: 401 (invalid/expired refresh token)

### POST /api/auth/logout
Request:
```json
{
  "refreshToken": "opaque-token"
}
```
Response (204): No content.

## Dependencies
- **User aggregate** (same context): reads user state for credential validation.

## Open questions
None.
