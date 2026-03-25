# Propose Resolution

## Context
Resolution bounded context. Proposing a resolution is the first step in determining the outcome of a porra.

## Description
After the resolution date passes, any group member (who bet on the porra) proposes the result. The proposal starts a 48-hour waiting period. If no one disputes within that window, the resolution is automatically accepted.

## Domain model

### Entities
- **Resolution** — aggregate root. Represents the resolution process for a single porra. Lifecycle: proposed → waiting (48h) → accepted | disputed.

### Value objects
- **ResolutionId** — UUID, generated server-side.
- **ProposedResult** — the result being proposed. Type depends on porra type:
  - BINARY: boolean
  - NUMERIC: BigDecimal (the actual real-world value)
  - MULTIPLE_CHOICE: String (one of the original options, or "NONE_OF_THE_ABOVE")
- **WaitingPeriod** — 48 hours from proposal time.

### Domain events
- `ResolutionProposed` — emitted when a result is proposed. Carries: resolutionId, porraId, groupId, proposerId, proposedResult, waitingEndsAt, occurredAt.
- `ResolutionAutoAccepted` — emitted when the 48h pass without dispute. Carries: resolutionId, porraId, acceptedResult, occurredAt.

## Rules and invariants

1. Only members who bet on the porra can propose a resolution.
2. The porra must be in PENDING_RESOLUTION status (resolution date has passed).
3. Only one active proposal can exist at a time per porra.
4. For MULTIPLE_CHOICE: the proposed result must be one of the original options OR "NONE_OF_THE_ABOVE".
5. For BINARY: the proposed result must be true or false.
6. For NUMERIC: the proposed result must be a valid number (the actual real-world outcome, not a bet).
7. The waiting period is exactly 48 hours.
8. If no dispute is filed within 48h, the resolution is automatically accepted.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| ProposeResolution | porraId, proposerId, proposedResult | resolutionId | 1-6, 9 |
| AutoAcceptResolution | resolutionId | void | 7, 8 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetResolution | porraId | resolution state (proposed result, proposer, time remaining, status) | Resolution detail view |

## Behavior

### Happy path (no dispute)
1. Member proposes a result for a porra in PENDING_RESOLUTION.
2. System validates the proposal.
3. System emits `ResolutionProposed`.
4. 48h waiting period begins. Countdown shown in UI via WebSocket.
5. No one disputes.
6. Scheduled job detects 48h have passed, emits `ResolutionAutoAccepted`.
7. Scoring distributes points based on the accepted result.
8. Betting marks porra as RESOLVED.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Porra not yet ready | Resolution date hasn't passed | Reject with "resolution not yet available" |
| Non-bettor proposes | Member didn't bet on this porra | Reject with "only bettors can propose" |
| Already proposed | Active proposal exists | Reject with "resolution already proposed" |
| Invalid result type | Numeric result for a binary porra | Reject with validation error |
| NONE_OF_THE_ABOVE for binary | Proposed for a binary porra | Reject — only valid for MULTIPLE_CHOICE |

## Acceptance criteria

1. GIVEN a porra in PENDING_RESOLUTION and a bettor WHEN ProposeResolution is executed with a valid result THEN `ResolutionProposed` is emitted with a 48h waiting period.
2. GIVEN a non-bettor WHEN ProposeResolution is executed THEN it is rejected.
3. GIVEN an active proposal exists WHEN another ProposeResolution is executed THEN it is rejected.
4. GIVEN a MULTIPLE_CHOICE porra WHEN ProposeResolution is executed with "NONE_OF_THE_ABOVE" THEN it is accepted.
5. GIVEN a BINARY porra WHEN ProposeResolution is executed with "NONE_OF_THE_ABOVE" THEN it is rejected.
6. GIVEN a proposal with no dispute after 48h WHEN AutoAcceptResolution runs THEN `ResolutionAutoAccepted` is emitted.

## API contract

### POST /api/porras/{porraId}/resolution
Request:
```json
{
  "proposedResult": "Barcelona"
}
```
Response (201):
```json
{
  "resolutionId": "uuid",
  "proposedResult": "Barcelona",
  "proposedBy": "uuid",
  "waitingEndsAt": "2026-03-27T14:30:00Z",
  "status": "WAITING"
}
```
Errors: 400 (not ready, already proposed), 403 (not a bettor), 422 (invalid result)

### GET /api/porras/{porraId}/resolution
Response (200):
```json
{
  "resolutionId": "uuid",
  "proposedResult": "Barcelona",
  "proposedBy": { "userId": "uuid", "email": "user@example.com" },
  "waitingEndsAt": "2026-03-27T14:30:00Z",
  "status": "WAITING",
  "timeRemaining": "PT36H15M"
}
```

### WebSocket: /porra/{porraId}
On `ResolutionProposed`:
```json
{
  "type": "RESOLUTION_PROPOSED",
  "proposedResult": "Barcelona",
  "proposedBy": "uuid",
  "waitingEndsAt": "2026-03-27T14:30:00Z"
}
```

## Dependencies
- **Betting context** (upstream): porra must be in PENDING_RESOLUTION, need list of bettors.
- **Settlement context** (downstream): `ResolutionAutoAccepted` triggers debt generation.
- **Dispute spec** (same context): disputes interrupt the waiting period.
- **Scheduled infrastructure**: 48h auto-accept job.

## Open questions
None.
