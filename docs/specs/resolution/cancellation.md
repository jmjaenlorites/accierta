# Porra Cancellation

## Context
Resolution bounded context. Cancellation is an alternative to resolution — it undoes the porra and refunds all bets.

## Description
Any bettor can propose cancelling a porra. The cancellation follows the same consensus mechanism as resolution: 48-hour waiting period, dispute, and vote. If accepted, all bets are refunded. This is separate from the creator's direct delete (which has a 15-minute window — see create-porra spec).

## Domain model

### Entities
- **Resolution** — existing aggregate. Cancellation is modeled as a special type of resolution where the result is "CANCELLED".

### Value objects
- **CancellationProposal** — contains: proposerId, reason (optional), proposedAt.

### Domain events
- `CancellationProposed` — emitted when someone proposes cancellation. Carries: resolutionId, porraId, groupId, proposerId, reason?, waitingEndsAt, occurredAt.
- `PorraCancelled` — emitted when cancellation is accepted. Carries: resolutionId, porraId, method (CONSENSUS | VOTE | TIEBREAK), occurredAt.

## Rules and invariants

1. Only bettors can propose cancellation.
3. The porra must be in CLOSED or PENDING_RESOLUTION status (not OPEN — if still open, the creator can delete it directly).
4. There cannot be an active resolution proposal AND an active cancellation proposal at the same time.
5. Cancellation follows the exact same consensus flow: 48h wait → auto-accept if no dispute → vote if disputed.
6. The proposer of the cancellation cannot vote if disputed. The disputer cannot vote.
7. If cancellation is accepted, ALL bets are refunded via Scoring.
8. If cancellation is rejected (dispute + vote), the porra remains in its current state and resolution can proceed normally.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| ProposeCancellation | porraId, proposerId, reason? | resolutionId | 1, 2, 3, 4 |

Note: after ProposeCancellation, the dispute/vote flow reuses the same commands from the dispute-and-vote spec (DisputeResolution, CastVote, TallyVotes). The only difference is that acceptance emits `PorraCancelled` instead of `ResolutionAccepted`.

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetCancellationStatus | porraId | cancellation state | Same as resolution status query |

## Behavior

### Happy path (cancellation accepted without dispute)
1. Bettor proposes cancellation with optional reason.
2. System emits `CancellationProposed`.
3. 48h waiting period.
4. No dispute → `PorraCancelled` emitted.
5. Scoring refunds all bets.
6. Betting marks porra as CANCELLED.

### Happy path (cancellation disputed and voted down)
1. Bettor proposes cancellation.
2. Another bettor disputes.
3. Voting: majority votes against cancellation.
4. Cancellation rejected — porra remains in current state.
5. Resolution can now proceed normally.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Active resolution proposal exists | Cancellation proposed while resolution is in 48h wait | Reject — cannot have both active simultaneously |
| Porra still OPEN | Cancellation proposed before deadline | Reject — use creator delete or wait until CLOSED |
| Cancellation rejected, then resolution proposed | Normal flow after failed cancellation | Allowed — resolution can proceed |
| Cancellation during voting on resolution | Resolution is in vote phase | Reject — active resolution process |

## Acceptance criteria

1. GIVEN a porra in PENDING_RESOLUTION and a bettor WHEN ProposeCancellation is executed THEN `CancellationProposed` is emitted.
2. GIVEN an active resolution proposal WHEN ProposeCancellation is executed THEN it is rejected.
3. GIVEN an OPEN porra WHEN ProposeCancellation is executed THEN it is rejected.
4. GIVEN a cancellation proposal with no dispute after 48h WHEN auto-accept runs THEN `PorraCancelled` is emitted and all bets are refunded.
5. GIVEN a cancellation proposal that was disputed and voted down WHEN tally runs THEN the cancellation is rejected and the porra remains in its current state.
6. GIVEN a rejected cancellation WHEN a resolution is later proposed THEN it is accepted normally.

## API contract

### POST /api/porras/{porraId}/cancellation
Request:
```json
{
  "reason": "El evento fue suspendido"
}
```
Response (201):
```json
{
  "resolutionId": "uuid",
  "type": "CANCELLATION",
  "waitingEndsAt": "2026-03-29T14:30:00Z",
  "status": "WAITING"
}
```
Errors: 400 (active proposal exists, porra still OPEN), 403 (not a bettor)

Note: dispute and vote endpoints are the same as in the dispute-and-vote spec. The resolution aggregate handles both resolution and cancellation proposals through the same flow.

### WebSocket: /porra/{porraId}
On `CancellationProposed`:
```json
{
  "type": "CANCELLATION_PROPOSED",
  "proposedBy": "uuid",
  "reason": "El evento fue suspendido",
  "waitingEndsAt": "2026-03-29T14:30:00Z"
}
```
On `PorraCancelled`:
```json
{
  "type": "PORRA_CANCELLED",
  "method": "CONSENSUS"
}
```

## Dependencies
- **Dispute and vote spec** (same context): reuses the consensus flow.
- **Betting context** (upstream): porra status check, list of bettors. (downstream): `PorraCancelled` marks porra as CANCELLED.
- **Settlement context** (downstream): `PorraCancelled` means no debts generated.

## Open questions
None.
