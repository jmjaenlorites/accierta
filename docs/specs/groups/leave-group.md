# Leave Group

## Context
Groups bounded context. Members can leave a group voluntarily, but only if they have no active porras.

## Description
A group member leaves the group. They must not have any active porras (open for betting, pending resolution, or in resolution process). Pending debts (owed or owing) are NOT erased by leaving — they remain until settled. If the member is later re-invited and rejoins, they start fresh (clean stats) but any unsettled debts from before still exist.

## Domain model

### Entities
- **Group** — existing aggregate. Leaving removes a member from the group.

### Value objects
- N/A

### Domain events
- `MemberLeft` — emitted when a member leaves. Carries: groupId, memberId, pendingDebtsOwed, pendingDebtsOwing, occurredAt.

## Rules and invariants

1. A member can only leave if they have no active porras in the group. Active means: porra is open for betting, betting closed but not yet resolved, or in resolution process (proposed, disputed, voting).
2. Pending debts are NOT affected by leaving. Debts owed to or by the member remain and can still be settled.
3. If the last member leaves, the group becomes empty (no deletion — it just has no members).
4. A member who left can rejoin via a new invitation. They start fresh (clean leaderboard stats) but unsettled debts from before persist.
5. The member's historical stats (past porras, results) remain visible in the group's history for integrity, but their profile is marked as "left".

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| LeaveGroup | groupId, memberId | void | 1, 2, 3 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| CanLeaveGroup | groupId, memberId | boolean + list of blocking porras | Pre-check before showing the leave option |

## Behavior

### Happy path
1. Member requests to leave the group.
2. System checks for active porras — none found.
3. System emits `MemberLeft` with pending debt summary.
4. Member is removed from group membership but debts persist.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Has active porras | Member bet on an unresolved porra | Reject with list of blocking porras |
| Has pending debts | Member owes or is owed money | Leave succeeds — debts persist and can still be settled |
| Last member | Only member leaves | Group becomes empty, no special handling |
| Rejoin after leaving | Member receives new invitation and accepts | Joins with fresh stats, but unsettled debts from before still visible |

## Acceptance criteria

1. GIVEN a member with no active porras WHEN LeaveGroup is executed THEN a `MemberLeft` event is emitted.
2. GIVEN a member with an active porra WHEN LeaveGroup is executed THEN it is rejected with the list of blocking porras.
3. GIVEN a member with pending debts WHEN LeaveGroup is executed THEN it succeeds and debts remain accessible for settlement.
4. GIVEN the last member of a group WHEN LeaveGroup is executed THEN the member leaves and the group becomes empty.
5. GIVEN a member who left WHEN they try to access the group THEN they are rejected with "not a member".
6. GIVEN a member who left and is re-invited WHEN they accept the invitation THEN they join with fresh leaderboard stats but unsettled debts persist.

## API contract

### GET /api/groups/{groupId}/can-leave
Response (200):
```json
{
  "canLeave": false,
  "blockingPorras": [
    { "porraId": "uuid", "title": "¿Quién gana la liga?", "status": "PENDING_RESOLUTION" }
  ]
}
```

### POST /api/groups/{groupId}/leave
Response (200):
```json
{
  "message": "Left group successfully"
}
```
Errors: 400 (has active porras), 403 (not a member)

## Dependencies
- **Betting context** (upstream): query to check if user has active porras.
- **Settlement context**: debts persist after leaving — no downstream action needed on `MemberLeft` except marking the member as "left" in projections.

## Open questions
None.
