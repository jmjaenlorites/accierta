# Create Porra

## Context
Betting bounded context. Creating a porra is the starting point of every betting cycle within a group.

## Description
Any group member creates a porra (prediction) for the group. A porra has a type (binary, numeric, multiple choice), a betting deadline, a resolution date, a stake amount, and a visibility setting. All members can then bet on it before the deadline.

## Domain model

### Entities
- **Porra** — aggregate root. Represents a single prediction within a group. Lifecycle: open → closed (deadline reached) → pending resolution → resolved | cancelled.

### Value objects
- **PorraId** — UUID, generated server-side.
- **PorraType** — enum: BINARY, NUMERIC, MULTIPLE_CHOICE.
- **PorraStatus** — enum: OPEN, CLOSED, PENDING_RESOLUTION, RESOLVED, CANCELLED.
- **Stake** — positive integer, the amount each member must bet to participate.
- **BettingDeadline** — datetime, after which no more bets are accepted.
- **ResolutionDate** — datetime, after which the resolution process can begin.
- **Tolerance** — percentage for numeric type. Default 15%. Stored as a decimal (0.15).
- **BetVisibility** — enum: VISIBLE (bets visible in real-time), HIDDEN (revealed at deadline). Immutable after creation.
- **BetModifiable** — boolean. If true, members can change their bet before the deadline. Default: true. Immutable after creation.
- **Option** — a possible choice for MULTIPLE_CHOICE type. String, non-empty.

### Domain events
- `PorraCreated` — emitted when a porra is created. Carries: porraId, groupId, creatorId, title, description, type, options (if multiple choice), tolerance (if numeric), stake, bettingDeadline, resolutionDate, visibility, betModifiable, occurredAt.
- `PorraDeletedByCreator` — emitted when the creator deletes the porra within the allowed window. Carries: porraId, groupId, occurredAt.

## Rules and invariants

1. Only group members can create porras.
2. Title is required, non-empty, max 200 characters.
3. Betting deadline must be in the future.
4. Resolution date must be after the betting deadline.
5. Stake must be a positive integer (> 0).
6. For MULTIPLE_CHOICE: at least 2 options required, all options must be unique and non-empty.
7. For NUMERIC: tolerance defaults to 15% (0.15) if not provided. Must be > 0 and <= 1 (100%).
8. For BINARY: no additional fields needed.
9. Visibility is set at creation and cannot be changed afterwards.
12. Bet modification setting (betModifiable) is set at creation, default true, immutable after creation. When true, members can change their prediction before the deadline.
10. The creator can delete the porra if: (a) no bets have been placed, OR (b) less than 15 minutes have passed since creation.
11. If deleted with existing bets, all bets are refunded via Scoring.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| CreatePorra | groupId, creatorId, title, description?, type, options?, tolerance?, stake, bettingDeadline, resolutionDate, visibility, betModifiable? | porraId | 1-9, 12 |
| DeletePorra | porraId, requesterId | void | 10, 11 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetPorra | porraId | Porra projection (all fields + bets if visible + status) | Porra detail view |
| ListGroupPorras | groupId, filters? | list of porra summaries | Group home — active, closed, resolved |

## Behavior

### Happy path
1. Member submits porra details.
2. System validates all fields and rules.
3. System emits `PorraCreated`.
4. Porra appears on the group home as OPEN.
5. Other members can now place bets.

### Happy path — delete within window
1. Creator requests deletion within 15 minutes of creation (or before any bet).
2. System emits `PorraDeletedByCreator`.
3. If any bets existed, Scoring refunds them.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Deadline in the past | bettingDeadline < now | Reject with validation error |
| Resolution before deadline | resolutionDate < bettingDeadline | Reject with validation error |
| Multiple choice with 1 option | Only 1 option provided | Reject — minimum 2 |
| Duplicate options | "A", "B", "A" | Reject — options must be unique |
| Numeric with tolerance 0 | tolerance = 0 | Reject — must be > 0 |
| Delete after 15 min with bets | Creator tries to delete at 20 min, bets exist | Reject — must use cancellation flow |
| Delete after 15 min, no bets | Creator tries to delete at 20 min, no bets | Allowed — rule 10a applies regardless of time |
| Non-creator tries to delete | Another member requests deletion | Reject — only creator can delete |

## Acceptance criteria

1. GIVEN a group member with valid porra details (BINARY) WHEN CreatePorra is executed THEN a `PorraCreated` event is emitted with type=BINARY.
2. GIVEN valid NUMERIC porra details without tolerance WHEN CreatePorra is executed THEN tolerance defaults to 0.15.
3. GIVEN valid MULTIPLE_CHOICE details with 3 options WHEN CreatePorra is executed THEN a `PorraCreated` event is emitted with the 3 options.
4. GIVEN a bettingDeadline in the past WHEN CreatePorra is executed THEN it is rejected.
5. GIVEN a resolutionDate before the bettingDeadline WHEN CreatePorra is executed THEN it is rejected.
6. GIVEN MULTIPLE_CHOICE with 1 option WHEN CreatePorra is executed THEN it is rejected.
7. GIVEN the creator within 15 minutes of creation WHEN DeletePorra is executed THEN `PorraDeletedByCreator` is emitted and any bets are refunded.
8. GIVEN the creator after 15 minutes with existing bets WHEN DeletePorra is executed THEN it is rejected.
9. GIVEN the creator after 15 minutes with no bets WHEN DeletePorra is executed THEN `PorraDeletedByCreator` is emitted.
10. GIVEN a non-creator member WHEN DeletePorra is executed THEN it is rejected.

## API contract

### POST /api/groups/{groupId}/porras
Request:
```json
{
  "title": "¿Cuántos goles habrá en el partido?",
  "description": "Partido del domingo",
  "type": "NUMERIC",
  "tolerance": 0.15,
  "stake": 50,
  "bettingDeadline": "2026-03-28T18:00:00Z",
  "resolutionDate": "2026-03-28T23:00:00Z",
  "visibility": "HIDDEN",
  "betModifiable": true
}
```
Response (201):
```json
{
  "porraId": "uuid",
  "title": "¿Cuántos goles habrá en el partido?",
  "type": "NUMERIC",
  "status": "OPEN"
}
```
Errors: 422 (validation), 403 (not a member)

### GET /api/groups/{groupId}/porras?status=OPEN
Response (200):
```json
[
  {
    "porraId": "uuid",
    "title": "¿Cuántos goles habrá en el partido?",
    "type": "NUMERIC",
    "status": "OPEN",
    "stake": 50,
    "bettingDeadline": "2026-03-28T18:00:00Z",
    "totalBets": 4
  }
]
```

### GET /api/porras/{porraId}
Response (200):
```json
{
  "porraId": "uuid",
  "groupId": "uuid",
  "creatorId": "uuid",
  "title": "¿Cuántos goles habrá en el partido?",
  "description": "Partido del domingo",
  "type": "NUMERIC",
  "tolerance": 0.15,
  "stake": 50,
  "bettingDeadline": "2026-03-28T18:00:00Z",
  "resolutionDate": "2026-03-28T23:00:00Z",
  "visibility": "HIDDEN",
  "status": "OPEN",
  "bets": [],
  "createdAt": "2026-03-25T10:00:00Z"
}
```
Note: `bets` is empty if visibility=HIDDEN and deadline hasn't passed. If visibility=VISIBLE, bets are shown in real-time.

### DELETE /api/porras/{porraId}
Response (204): No content.
Errors: 403 (not creator), 400 (outside deletion window)

## Dependencies
- **Groups context** (upstream): membership check.
- **Scoring context** (downstream): `PorraDeletedByCreator` triggers refund.
- **Place bet spec** (same context): created porras can receive bets.

## Open questions
None.
