# User Registration

## Context
Identity bounded context. This is the entry point to the system — a user must register before interacting with any other feature. Registration can happen standalone or through a group invitation link.

## Description
A new user creates an account by providing an email and password. The system sends a verification email via Resend. The account remains inactive until the email is verified. If the registration originated from a group invitation link, the user is automatically added to the group after verification.

## Domain model

### Entities
- **User** — represents a registered person. Lifecycle: created (unverified) → email verified (active). The User aggregate root.

### Value objects
- **Email** — validated email address (format, max length). Case-insensitive (stored lowercase).
- **HashedPassword** — bcrypt hash of the user's password. Never stored or transmitted in plain text.
- **VerificationToken** — one-time token sent via email. Has an expiration time.
- **UserId** — UUID, generated server-side.

### Domain events
- `UserRegistered` — emitted when a user submits the registration form. Carries: userId, email, invitationToken (nullable), occurredAt.
- `EmailVerified` — emitted when the user clicks the verification link. Carries: userId, occurredAt.
- `VerificationEmailResent` — emitted when the user requests a new verification email. Carries: userId, email, occurredAt.

## Rules and invariants

1. Email must be unique across all users (case-insensitive).
2. Password must be at least 8 characters.
3. A user cannot log in or perform any action until their email is verified.
4. Verification tokens expire after 24 hours.
5. A new verification token invalidates any previous one for the same user.
6. If registration includes an invitation token, the `UserRegistered` event carries it so the Groups context can process auto-join.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| RegisterUser | email, password, invitationToken? | userId | 1, 2 |
| VerifyEmail | verificationToken | void | 3, 4 |
| ResendVerification | email | void | 4, 5 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetUserByEmail | email | User projection (id, email, verified, createdAt) | Used by login flow |
| IsEmailRegistered | email | boolean | Used during registration to check uniqueness |

## Behavior

### Happy path
1. User submits email + password (optionally coming from an invitation link).
2. System validates email uniqueness and password strength.
3. System creates user aggregate, emits `UserRegistered`.
4. System sends verification email via Resend with a one-time link.
5. User clicks the link.
6. System validates the token and its expiration.
7. System emits `EmailVerified`.
8. If invitation token was present, Groups context reacts to `UserRegistered` + `EmailVerified` and auto-joins the user.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Duplicate email | Email already registered | Reject with "email already in use" error |
| Weak password | Password < 8 characters | Reject with validation error |
| Expired verification token | Token older than 24h | Reject, prompt user to request a new one |
| Already verified | User clicks verification link again | Idempotent — return success, no new event |
| Resend verification | User didn't receive email | Generate new token (invalidates old), send new email |
| Unverified user tries to act | Any action before email verification | Reject with "email not verified" error |

## Acceptance criteria

1. GIVEN a valid email and password WHEN RegisterUser is executed THEN a `UserRegistered` event is emitted with the user's id and email.
2. GIVEN an email that already exists WHEN RegisterUser is executed THEN registration is rejected with a duplicate email error.
3. GIVEN a password shorter than 8 characters WHEN RegisterUser is executed THEN registration is rejected with a validation error.
4. GIVEN a valid verification token that has not expired WHEN VerifyEmail is executed THEN an `EmailVerified` event is emitted.
5. GIVEN a verification token older than 24 hours WHEN VerifyEmail is executed THEN verification is rejected with an expired token error.
6. GIVEN a user whose email is already verified WHEN VerifyEmail is executed THEN the operation succeeds idempotently without emitting a new event.
7. GIVEN an unverified user WHEN ResendVerification is executed THEN a new token is generated, the old one is invalidated, and a new email is sent.
8. GIVEN a registration with an invitation token WHEN `UserRegistered` is emitted THEN the event carries the invitation token for the Groups context.

## API contract

### POST /api/auth/register
Request:
```json
{
  "email": "user@example.com",
  "password": "securepassword",
  "invitationToken": "optional-token"
}
```
Response (201):
```json
{
  "userId": "uuid",
  "message": "Verification email sent"
}
```
Errors: 409 (duplicate email), 422 (validation error)

### POST /api/auth/verify-email
Request:
```json
{
  "token": "verification-token"
}
```
Response (200):
```json
{
  "message": "Email verified"
}
```
Errors: 400 (invalid/expired token)

### POST /api/auth/resend-verification
Request:
```json
{
  "email": "user@example.com"
}
```
Response (200):
```json
{
  "message": "Verification email sent"
}
```
Errors: 404 (email not found), 400 (already verified)

## Dependencies
- **Resend** (infrastructure): for sending verification emails.
- **Groups context** (downstream): consumes `UserRegistered` event when it carries an invitation token.

## Open questions
None.
