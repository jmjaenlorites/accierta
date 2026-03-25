# Porra Lifecycle

## Context
Betting bounded context. Manages the automatic state transitions of a porra based on time.

## Description
A porra transitions through states automatically based on its configured dates. When the betting deadline passes, the porra closes and hidden bets are revealed. When the resolution date arrives, the porra becomes eligible for resolution proposals.

## Domain model

### Entities
- **Porra** — existing aggregate. Lifecycle transitions are state changes on the porra.

### Value objects
- **PorraStatus** — enum: OPEN → CLOSED → PENDING_RESOLUTION → RESOLVED | CANCELLED.

### Domain events
- `BettingDeadlineReached` — emitted when the betting deadline passes and there are bets. Carries: porraId, groupId, bets (all bets revealed), occurredAt.
- `PorraAutoCancelled` — emitted when the betting deadline passes with fewer than 2 bets. Carries: porraId, groupId, occurredAt.

## Rules and invariants

1. A porra transitions from OPEN to CLOSED exactly when the betting deadline is reached.
2. When a porra closes, ALL bets are revealed regardless of visibility setting.
3. A porra transitions from CLOSED to PENDING_RESOLUTION when the resolution date is reached.
4. State transitions are one-directional. A CLOSED porra cannot go back to OPEN.
5. The BettingDeadlineReached event carries the full list of bets — this is how hidden bets become visible.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| CloseBetting | porraId | void | 1, 2, 5 |
| EnableResolution | porraId | void | 3 |

Note: these commands are triggered by a scheduled job (time-based), not by user action.

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetPorrasByStatus | groupId, status | list of porras | Filter by lifecycle state |
| GetPorrasNearingDeadline | groupId, within | list of porras closing within X hours | For "24h left" notifications |

## Behavior

### Happy path
1. Scheduled job detects porra's betting deadline has passed.
2. System executes CloseBetting → emits `BettingDeadlineReached`.
3. If visibility was HIDDEN, all bets are now revealed.
4. WebSocket pushes revealed bets to `/porra/{id}`.
5. Notification sent: "Betting closed for [porra title]".
6. Later, scheduled job detects resolution date has passed.
7. System executes EnableResolution → porra status becomes PENDING_RESOLUTION.
8. Notification sent: "Resolution is now open for [porra title]".

### 24h notification
1. Scheduled job detects porra deadline is within 24 hours.
2. Notification sent to group members who haven't bet yet: "24h left to bet on [porra title]".

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Fewer than 2 bets by deadline | Deadline passes, 0 or 1 bet | Porra is automatically cancelled and bets refunded. No resolution phase needed. Minimum 2 bets required for a valid porra. |
| Porra deleted before deadline | Creator deletes it | PorraDeletedByCreator — no lifecycle transitions |
| Server downtime during deadline | Scheduled job missed the exact time | On next run, detect overdue porras and process them. Events carry the actual deadline time, not processing time |

## Acceptance criteria

1. GIVEN an OPEN porra with 2+ bets whose betting deadline has passed WHEN CloseBetting is executed THEN `BettingDeadlineReached` is emitted and status becomes CLOSED.
6. GIVEN an OPEN porra with 0 bets whose betting deadline has passed WHEN CloseBetting is executed THEN `PorraAutoCancelled` is emitted and status becomes CANCELLED.
7. GIVEN an OPEN porra with exactly 1 bet whose betting deadline has passed WHEN CloseBetting is executed THEN `PorraAutoCancelled` is emitted, the single bet is refunded, and status becomes CANCELLED.
2. GIVEN a HIDDEN porra that just closed WHEN `BettingDeadlineReached` is emitted THEN the event carries all bets and they are pushed via WebSocket.
3. GIVEN a CLOSED porra whose resolution date has passed WHEN EnableResolution is executed THEN status becomes PENDING_RESOLUTION.
4. GIVEN a porra with a deadline in 23 hours WHEN the 24h check runs THEN members who haven't bet receive a notification.
5. GIVEN a porra that was deleted before deadline WHEN the scheduled job runs THEN the porra is skipped (status is CANCELLED/DELETED).

## API contract

No dedicated API endpoints — lifecycle transitions are automatic (scheduled jobs).

The porra detail endpoint (GET /api/porras/{porraId}) reflects the current status. Bets are visible after CLOSED regardless of original visibility setting.

### WebSocket: /porra/{porraId}
On `BettingDeadlineReached`:
```json
{
  "type": "BETTING_CLOSED",
  "bets": [
    { "memberId": "uuid", "memberEmail": "user@example.com", "prediction": "B" },
    { "memberId": "uuid", "memberEmail": "other@example.com", "prediction": "A" }
  ]
}
```

### PWA Push Notifications
- "24h left to bet on [title]" → members who haven't bet
- "Betting closed on [title]" → all group members
- "[title] is ready for resolution" → all group members

## Dependencies
- **Scheduled infrastructure** (infrastructure): cron/scheduled job to check deadlines.
- **Place bet spec** (same context): bets are revealed at deadline.
- **Resolution context** (downstream): `BettingDeadlineReached` enables resolution proposals.
- **WebSocket** (infrastructure): real-time bet reveal.
- **PWA push** (infrastructure): notifications.

## Open questions
None.
