# Place Bet

## Context
Betting bounded context. Placing a bet is how members participate in a porra.

## Description
A group member places a bet on an open porra before the betting deadline. The bet type depends on the porra type: yes/no for binary, a decimal number for numeric, or one of the predefined options for multiple choice. All members bet the same stake amount (defined when the porra was created). If the porra has betModifiable=true, the member can change their prediction before the deadline. There is no balance check — bets generate real debts on resolution, not point deductions.

## Domain model

### Entities
- **Porra** — existing aggregate. A bet is part of the Porra aggregate (the porra owns its bets as an invariant boundary — e.g., no duplicate bets per member).

### Value objects
- **Bet** — value object within the Porra aggregate. Contains: memberId, prediction, placedAt.
- **Prediction** — union type depending on porra type:
  - BINARY: boolean (true = yes, false = no)
  - NUMERIC: BigDecimal (exact value with decimals)
  - MULTIPLE_CHOICE: String (must be one of the porra's options)

### Domain events
- `BetPlaced` — emitted when a bet is placed. Carries: porraId, groupId, memberId, prediction, stake, occurredAt.
- `BetModified` — emitted when a bet is changed (only if betModifiable=true). Carries: porraId, memberId, oldPrediction, newPrediction, occurredAt.

## Rules and invariants

1. Only group members can bet.
2. A member can only bet once per porra — unless betModifiable=true, in which case they can change their prediction (not withdraw) before the deadline.
3. The porra must be in OPEN status (deadline not passed).
4. For BINARY: prediction must be true or false.
5. For NUMERIC: prediction must be a valid number (decimals allowed).
6. For MULTIPLE_CHOICE: prediction must be one of the porra's defined options (exact match).
7. The creator of the porra can also bet on their own porra.
8. If visibility is VISIBLE, the bet (who, when, and prediction) is immediately visible to all group members via WebSocket.
9. If visibility is HIDDEN, the bet's **prediction is not revealed** until the deadline passes. However, **who bet and when** is always visible in real-time.
10. When betModifiable=true, modifying a bet emits `BetModified`. The old prediction is replaced. If visibility is VISIBLE, the change is broadcast.
11. When betModifiable=false, any attempt to change a bet is rejected.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| PlaceBet | porraId, memberId, prediction | void | 1-6, 7 |
| ModifyBet | porraId, memberId, newPrediction | void | 2, 3, 4-6, 10, 11 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetPorraBets | porraId | list of bets (or hidden indicator) | Respects visibility setting |
| HasMemberBet | porraId, memberId | boolean | Pre-check in UI |

## Behavior

### Happy path (place bet)
1. Member selects their prediction and confirms.
2. System validates membership, porra status, no duplicate bet, prediction format.
3. System emits `BetPlaced`.
4. If visibility=VISIBLE, WebSocket pushes the bet to `/porra/{id}`.

### Happy path (modify bet)
1. Member changes their prediction (porra has betModifiable=true).
2. System validates porra is still OPEN and prediction format.
3. System emits `BetModified` with old and new prediction.
4. If visibility=VISIBLE, WebSocket pushes the updated bet.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Already bet, not modifiable | Member tries to bet again, betModifiable=false | Reject with "already bet on this porra" |
| Already bet, modifiable | Member tries to bet again, betModifiable=true | Treated as a modification — emits BetModified |
| Deadline passed | Porra status is CLOSED | Reject with "betting deadline passed" |
| Modify when not modifiable | Member tries to modify, betModifiable=false | Reject with "bet modification not allowed" |
| Invalid option | Multiple choice, prediction not in options | Reject with "invalid option" |
| Creator bets | Creator places a bet | Allowed — same rules as anyone else |
| Hidden visibility | Bet placed, visibility=HIDDEN | Who bet and when is broadcast. Prediction value is hidden until deadline. |

## Acceptance criteria

1. GIVEN an open BINARY porra and a member with sufficient balance WHEN PlaceBet is executed with prediction=true THEN a `BetPlaced` event is emitted.
2. GIVEN an open NUMERIC porra WHEN PlaceBet is executed with prediction=3.5 THEN a `BetPlaced` event is emitted with prediction=3.5.
3. GIVEN an open MULTIPLE_CHOICE porra with options ["A", "B", "C"] WHEN PlaceBet is executed with prediction="B" THEN a `BetPlaced` event is emitted.
4. GIVEN a MULTIPLE_CHOICE porra WHEN PlaceBet is executed with prediction="D" (not in options) THEN it is rejected.
5. GIVEN a member who already bet and betModifiable=false WHEN PlaceBet is executed again THEN it is rejected with "already bet".
6. GIVEN a member who already bet and betModifiable=true WHEN PlaceBet is executed again THEN a `BetModified` event is emitted with old and new prediction.
7. GIVEN a porra with status CLOSED WHEN PlaceBet is executed THEN it is rejected with "deadline passed".
8a. GIVEN betModifiable=false WHEN ModifyBet is executed THEN it is rejected.
8. GIVEN a porra with visibility=VISIBLE WHEN a bet is placed THEN the full bet (who, when, prediction) is pushed via WebSocket.
9. GIVEN a porra with visibility=HIDDEN WHEN a bet is placed THEN who bet and when is pushed via WebSocket, but the prediction is omitted.

## API contract

### POST /api/porras/{porraId}/bets
Request:
```json
{
  "prediction": "B"
}
```
Note: prediction is a string for MULTIPLE_CHOICE, a number for NUMERIC, or a boolean for BINARY. The API accepts any JSON value and validates against the porra type.

Response (201):
```json
{
  "message": "Bet placed",
  "porraId": "uuid",
  "prediction": "B"
}
```
Errors: 400 (already bet, invalid prediction), 403 (not a member, deadline passed), 422 (insufficient balance)

### WebSocket: /porra/{porraId}
On `BetPlaced` (if visibility=VISIBLE):
```json
{
  "type": "BET_PLACED",
  "memberId": "uuid",
  "memberEmail": "user@example.com",
  "prediction": "B",
  "placedAt": "2026-03-25T14:30:00Z"
}
```
On `BetPlaced` (if visibility=HIDDEN):
```json
{
  "type": "BET_PLACED",
  "memberId": "uuid",
  "memberEmail": "user@example.com",
  "prediction": null,
  "placedAt": "2026-03-25T14:30:00Z"
}
```

## Dependencies
- **Groups context** (upstream): membership check.
- **Create porra spec** (same context): porra must exist and be OPEN. betModifiable setting.
- **Settlement context** (downstream): `BetPlaced` is used to generate debts after resolution (no immediate deduction).

## Open questions
None.
