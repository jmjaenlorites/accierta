# Password Reset

## Context
Identity bounded context. Users who forget their password need a way to set a new one without being logged in.

## Description
A user requests a password reset by providing their email. The system sends a reset link via Resend with a one-time token. The user clicks the link and sets a new password.

## Domain model

### Entities
- **User** — existing aggregate. Password reset changes the stored password hash.

### Value objects
- **ResetToken** — one-time token sent via email. Has an expiration time.
- **HashedPassword** — new bcrypt hash replacing the old one.

### Domain events
- `PasswordResetRequested` — emitted when a user requests a reset. Carries: userId, email, occurredAt.
- `PasswordReset` — emitted when the password is successfully changed. Carries: userId, occurredAt.

## Rules and invariants

1. Reset tokens expire after 1 hour.
2. A new reset token invalidates any previous one for the same user.
3. The new password must meet the same requirements as registration (min 8 characters).
4. After a successful reset, all existing refresh tokens for the user are invalidated (force re-login on all devices).
5. Requesting a reset for a non-existent email returns success (don't leak whether an email is registered).

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| RequestPasswordReset | email | void | 2, 5 |
| ResetPassword | resetToken, newPassword | void | 1, 3, 4 |

### Queries (read operations)
N/A — this flow is entirely command-driven.

## Behavior

### Happy path
1. User submits their email to request a reset.
2. System generates a reset token (1h expiry), invalidates any previous token.
3. System sends reset email via Resend.
4. User clicks the link, submits a new password.
5. System validates the token and password requirements.
6. System updates the password hash, emits `PasswordReset`.
7. All refresh tokens for this user are invalidated.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Unknown email | Email not registered | Return success anyway (don't leak info) |
| Expired token | Token older than 1h | Reject, prompt user to request a new one |
| Already used token | Token was already consumed | Reject with invalid token error |
| Weak new password | Password < 8 characters | Reject with validation error |

## Acceptance criteria

1. GIVEN a registered email WHEN RequestPasswordReset is executed THEN a `PasswordResetRequested` event is emitted and a reset email is sent.
2. GIVEN an unknown email WHEN RequestPasswordReset is executed THEN the operation returns success without sending an email.
3. GIVEN a valid, non-expired reset token and a valid new password WHEN ResetPassword is executed THEN a `PasswordReset` event is emitted and the password is updated.
4. GIVEN an expired reset token WHEN ResetPassword is executed THEN it is rejected with an expired token error.
5. GIVEN a new password shorter than 8 characters WHEN ResetPassword is executed THEN it is rejected with a validation error.
6. GIVEN a successful password reset WHEN the user's sessions are checked THEN all existing refresh tokens are invalidated.

## API contract

### POST /api/auth/request-password-reset
Request:
```json
{
  "email": "user@example.com"
}
```
Response (200):
```json
{
  "message": "If this email is registered, a reset link has been sent"
}
```

### POST /api/auth/reset-password
Request:
```json
{
  "token": "reset-token",
  "newPassword": "newsecurepassword"
}
```
Response (200):
```json
{
  "message": "Password updated successfully"
}
```
Errors: 400 (invalid/expired token), 422 (weak password)

## Dependencies
- **Resend** (infrastructure): for sending reset emails.
- **Login spec** (same context): reset invalidates all refresh tokens.

## Open questions
None.
