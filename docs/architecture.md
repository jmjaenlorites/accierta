# Architecture Overview

## System overview

Accierta is a modular monolith built with Java 21+ / Spring Boot 3.x. It follows DDD with Hexagonal Architecture internally and hand-made Event Sourcing for persistence. Each bounded context is a Spring Modulith module that communicates with others exclusively through domain events.

```
┌─────────────────────────────────────────────────────────┐
│                        Accierta                           │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ Identity │  │  Groups  │  │ Betting  │              │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘              │
│       │              │              │                    │
│       │    domain events only       │                    │
│       │              │              │                    │
│  ┌────┴─────┐  ┌────┴──────┐                           │
│  │Resolution│  │Settlement │                           │
│  └──────────┘  └───────────┘                           │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │              Shared Infrastructure               │   │
│  │  PostgreSQL (event store + projections)          │   │
│  │  Spring WebSocket                                │   │
│  │  Resend (email)                                  │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
         │                              │
    REST API                     WebSocket
         │                              │
┌─────────────────────────────────────────────────────────┐
│                React PWA (Frontend)                      │
└─────────────────────────────────────────────────────────┘
```

## Deployment topology

Accierta is deployed as a **decoupled web + API system**.

- The React PWA is a static frontend deployed independently from the backend.
- The Spring Boot modular monolith is deployed as the public API application.
- PostgreSQL is deployed alongside the backend, one database per environment.
- Production and staging mirror each other with separate domains, infrastructure, data, and secrets.

This decision is documented in [ADR-008](./adr/008-web-and-api-deployment-topology.md).
The CI/CD model for this topology is documented in [ADR-009](./adr/009-monorepo-ci-cd.md).

### Public entrypoints

| Environment | Web frontend | Backend API | Notes |
|-------------|--------------|-------------|-------|
| Production | `https://accierta.com` | `https://api.accierta.com` | Main user-facing environment |
| Staging | `https://staging.accierta.com` | `https://api-staging.accierta.com` | Pre-production validation |

Production also accepts `https://www.accierta.com` as an approved browser origin for CORS and WebSocket handshakes, even if `accierta.com` remains the canonical web domain.

### Infrastructure responsibility split

| Concern | Platform | Why |
|---------|----------|-----|
| Static frontend hosting | Cloudflare Pages | Edge delivery, asset caching, TLS, preview-friendly workflow |
| Backend runtime | Coolify | Container deployment, environment variables, logs, secret management |
| Database | Coolify-managed PostgreSQL | Stateful service close to backend runtime |
| DNS and proxy | Cloudflare | Public DNS, TLS, request proxying, WebSocket support |

### Client access model

All clients access the backend directly through its public API domain:
- The React PWA uses environment-specific API and WebSocket base URLs.
- Future native iOS and Android clients use the same API boundary.
- The backend is not hidden behind the web frontend in production.

This is intentionally different from the local Docker Compose setup. In local development, the frontend container proxies `/api` and `/ws` to simplify local wiring. In deployed environments, the web app and the API are separate public applications because the system must support more than one client type.

### Runtime configuration

The deployed system relies on explicit runtime configuration instead of same-origin assumptions:
- Frontend:
  - `VITE_API_BASE_URL`
  - `VITE_WS_BASE_URL`
- Backend:
  - `SPRING_PROFILES_ACTIVE`
  - `ACCIERTA_SECURITY_JWT_SECRET`
  - `ACCIERTA_GROUPS_INVITATION_BASE_URL`
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

Environment profiles define approved browser origins:
- `prod`: `https://accierta.com` and `https://www.accierta.com`
- `staging`: `https://staging.accierta.com`

## Bounded context map

### Identity
**Responsibility:** user registration, email verification, login, password reset, sessions.

**Aggregates:** User

**Events produced:**
- `UserRegistered`
- `EmailVerified`
- `PasswordReset`

**Events consumed:** none

**Depended on by:** all other contexts (user identity)

---

### Groups
**Responsibility:** group creation (with configurable currency), invitation links, membership management, leaving groups.

**Aggregates:** Group

**Events produced:**
- `GroupCreated` (carries currency)
- `InvitationSent`
- `MemberJoined`
- `MemberLeft`

**Events consumed:**
- `UserRegistered` → auto-join group if registration came from an invite link

**Depends on:** Identity (user exists)

**Depended on by:** Betting, Settlement (group membership, currency)

---

### Betting
**Responsibility:** porra creation (3 types), bet placement and modification, porra lifecycle (open → closed → pending resolution → resolved/cancelled).

**Aggregates:** Porra

**Events produced:**
- `PorraCreated` (carries stake, betModifiable, visibility)
- `BetPlaced`
- `BetModified`
- `BettingDeadlineReached`
- `PorraAutoCancelled` (< 2 bets at deadline)
- `PorraDeletedByCreator`

**Events consumed:**
- `ResolutionAccepted` → mark porra as resolved
- `PorraCancelled` → mark porra as cancelled

**Depends on:** Groups (membership check)

---

### Resolution
**Responsibility:** proposing results, 48h waiting period, disputes, voting, consensus, cancellation proposals.

**Aggregates:** Resolution

**Events produced:**
- `ResolutionProposed`
- `ResolutionDisputed`
- `VoteCast`
- `ResolutionAccepted`
- `PorraCancelled`

**Events consumed:**
- `BettingDeadlineReached` → resolution phase can begin
- `PorraCreated` → knows porra type and options for validation

**Depends on:** Betting (who bet, porra type/options)

---

### Settlement
**Responsibility:** debt generation from resolutions, debt tracking, payment requests, payment confirmation, leaderboard, stats, history.

**Aggregates:** Debt

**Events produced:**
- `DebtGenerated`
- `PaymentRequested`
- `DebtSettled`

**Events consumed:**
- `MemberJoined` → add member to leaderboard
- `MemberLeft` → mark member as "left" in leaderboard (debts persist)
- `ResolutionAccepted` → generate debts (one per loser→winner pair)
- `PorraCancelled` → no debts generated
- `PorraAutoCancelled` → no debts generated
- `PorraDeletedByCreator` → no debts generated

**Depends on:** Groups (currency), Betting + Resolution (events)

---

## Context relationships

```
Identity ──publishes──→ UserRegistered
    │
    ▼ (user exists)
Groups ──publishes──→ MemberJoined, MemberLeft, GroupCreated
    │
    ├──→ Settlement (MemberJoined → add to leaderboard)
    │
    ▼ (membership)
Betting ──publishes──→ PorraCreated, BetPlaced, BetModified, BettingDeadlineReached, PorraAutoCancelled
    │
    ├──→ Resolution (BettingDeadlineReached → enable resolution)
    │
    ▼
Resolution ──publishes──→ ResolutionAccepted, PorraCancelled
    │
    ├──→ Betting (ResolutionAccepted → mark resolved)
    └──→ Settlement (ResolutionAccepted → generate debts)
                    (PorraCancelled → no debts)
```

### Relationship types

| From | To | Type | Mechanism |
|------|----|------|-----------|
| Identity → Groups | Upstream/Downstream | Groups reacts to `UserRegistered` for invite-based registration |
| Groups → Settlement | Upstream/Downstream | Settlement reacts to `MemberJoined` to add to leaderboard |
| Groups → Betting | Upstream/Downstream | Betting checks membership via a query (sync read from Groups projection) |
| Betting → Resolution | Upstream/Downstream | Resolution reacts to `BettingDeadlineReached` |
| Resolution → Betting | Upstream/Downstream | Betting reacts to `ResolutionAccepted` / `PorraCancelled` |
| Resolution → Settlement | Upstream/Downstream | Settlement reacts to `ResolutionAccepted` to generate debts |

### Cross-context queries

Some contexts need to read data from other contexts. This is done through **read-only projections**, never by accessing another context's aggregates or event store directly.

| Consumer | Needs from | Data | How |
|----------|-----------|------|-----|
| Betting | Groups | Is user a member of this group? | Query Groups projection |
| Resolution | Betting | Who bet on this porra? What type/options? | Query Betting projection |

## Key data flows

### Flow 1: Create group and invite friends

```
User creates group (name, currency)
  → GroupCreated event (carries currency, e.g. "€" or "🍺")

User sends invite (email via Resend)
  → InvitationSent event

Friend clicks link, registers
  → UserRegistered event
  → Groups auto-joins member (MemberJoined event)
  → Settlement adds member to leaderboard
```

### Flow 2: Create porra and place bets

```
Member creates porra
  → PorraCreated event (type, options, deadline, resolution date, stake, visibility, betModifiable)

Member places bet
  → BetPlaced event (no balance check — debts are generated on resolution, not on bet)

Member modifies bet (if betModifiable=true and deadline not passed)
  → BetModified event

Deadline passes (with 2+ bets)
  → BettingDeadlineReached event
  → Hidden bets revealed (if visibility was off)
  → Resolution phase enabled

Deadline passes (with 0 or 1 bet)
  → PorraAutoCancelled event
  → Any single bet has no financial impact
```

### Flow 3: Resolution with consensus

```
Member proposes result
  → ResolutionProposed event
  → 48h waiting period starts

CASE A: No dispute within 48h
  → ResolutionAccepted event
  → Settlement generates debts (one per loser→winner pair)
  → Betting marks porra as resolved

CASE B: Someone disputes
  → ResolutionDisputed event
  → Voting opens (only bettors, excluding proposer and disputer)
  → VoteCast events
  → Majority decides (tie: random voter breaks it)
  → ResolutionAccepted event
  → Settlement generates debts

CASE C: Resolution is "none of the above" (multiple choice)
  → ResolutionAccepted with no winner
  → No debts generated — nobody wins, nobody loses
```

### Flow 4: Cancellation

```
Member proposes cancellation
  → CancellationProposed event
  → Same consensus flow as resolution (48h wait, dispute, vote)

If accepted:
  → PorraCancelled event
  → No debts generated
  → Betting marks porra as cancelled
```

### Flow 5: Creator direct delete (within 15 min or no bets)

```
Creator deletes porra (no bets yet OR <15 min since creation)
  → PorraDeletedByCreator event
  → No debts generated
```

### Flow 6: Debt settlement

```
Porra resolved, debts generated
  → DebtGenerated events (Bob→Alice 10€, Carol→Alice 10€)

Bob pays Alice via Bizum outside the app
  → Bob requests payment confirmation (PaymentRequested)
  → Alice confirms receipt (DebtSettled)
  → Debt status: PAID

Consolidated view: if Alice owes Bob 5€ from another porra,
  net position shows "Bob owes Alice 5€" (10€ - 5€)
```

## Event store schema

```sql
CREATE TABLE events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id  UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    payload       JSONB NOT NULL,
    version       BIGINT NOT NULL,
    occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (aggregate_id, version)  -- optimistic concurrency
);

CREATE INDEX idx_events_aggregate ON events (aggregate_id, version);
CREATE INDEX idx_events_type ON events (event_type, occurred_at);
```

## Real-time (WebSocket)

WebSocket channels push updates to connected clients:

| Channel | Events pushed | Triggered by |
|---------|--------------|-------------|
| `/group/{id}/porras` | New porra, status changes | PorraCreated, BettingDeadlineReached, PorraAutoCancelled, ResolutionAccepted, PorraCancelled |
| `/porra/{id}` | New bets (if visible), modifications, resolution updates | BetPlaced, BetModified, ResolutionProposed, ResolutionDisputed, VoteCast |
| `/group/{id}/leaderboard` | Ranking updates | DebtGenerated, DebtSettled |
| `/group/{id}/debts` | New debts, payment requests, settlements | DebtGenerated, PaymentRequested, DebtSettled |
| `/user/{id}/notifications` | Personal notifications | All events relevant to the user |

## PWA push notifications

Triggered server-side when relevant events occur:

- Porra created in your group
- 24h before betting deadline
- Betting deadline reached
- Resolution proposed
- Resolution disputed
- Resolution accepted / debts generated
- Porra cancelled
- Someone requested payment confirmation for a debt you owe
- Someone settled a debt they owed you
