# ADR-007: Hand-made authentication with email + password

## Status
Accepted (revised — originally magic link only, changed to email + password)

## Context
The app needs authentication for friend groups. Options: OAuth (Google/Apple), magic link only, email + password, or a third-party service (Clerk, Auth0).

The target users are small friend groups. Low friction is critical — if registration is annoying, people won't join the group.

## Decision
Hand-made email + password authentication. Resend as email delivery service (free tier: 3,000/month, 100/day).

Flows:
- **Register:** email + password. Validation email sent via Resend. Account inactive until email confirmed.
- **Login:** email + password. JWT session.
- **Reset password:** one-time token sent via Resend.
- **Group invitation:** invite link sent via Resend. If not registered, redirects to registration and auto-joins the group after confirming.

Email is used only for transactional flows (registration, reset, invitations). Notifications go through PWA push, not email.

## Consequences
- **Standard auth flow:** well-understood, easy to implement. Password hashing (bcrypt), JWT sessions, email verification token.
- **Resend dependency:** free tier (100 emails/day) is more than sufficient for small friend groups. Only used for registration, reset, and invitations — not notifications.
- **Password management is our responsibility:** secure hashing, reset flow, validation.
- **Session management is our responsibility:** JWT issuance, refresh, and revocation.
- **Invite flow is seamless:** one link does both registration and group joining.
