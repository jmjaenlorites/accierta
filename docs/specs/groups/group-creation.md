# Group Creation

## Context
Groups bounded context. A group is the fundamental social unit — all porras, bets, and leaderboards exist within a group.

## Description
A verified user creates a new group by providing a name and a currency (defaults to €). The creator is automatically added as the first member. There is no "owner" role — all members have equal permissions. There is no initial balance — the app tracks real debts between members, not fictional points.

## Domain model

### Entities
- **Group** — aggregate root. Represents a closed group of friends. Lifecycle: created → active (members can join/leave, porras can be created).

### Value objects
- **GroupId** — UUID, generated server-side.
- **GroupName** — non-empty string, max 100 characters.
- **Currency** — symbol or emoji representing what's being bet. Default: "€". Examples: "€", "$", "🍺", "🍕".
- **MemberId** — references a UserId from the Identity context.

### Domain events
- `GroupCreated` — emitted when a group is created. Carries: groupId, groupName, currency, creatorId, occurredAt.
- `MemberJoined` — emitted when the creator is auto-added. Carries: groupId, memberId, occurredAt.

## Rules and invariants

1. Group name must be non-empty and max 100 characters.
2. Currency must be non-empty, max 10 characters (to allow emojis).
3. The creator is automatically added as the first member.
4. Only verified users can create groups.
5. Currency is immutable after group creation.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| CreateGroup | name, currency?, creatorId | groupId | 1, 2, 3, 4, 5 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetGroup | groupId | Group projection (id, name, currency, members, createdAt) | Used by group home view |
| ListUserGroups | userId | List of group summaries | Groups the user belongs to |

## Behavior

### Happy path
1. Verified user submits group name and optionally a currency.
2. System validates name and currency (defaults to "€").
3. System creates group aggregate, emits `GroupCreated`.
4. System adds creator as first member, emits `MemberJoined`.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Empty name | Name is blank or whitespace | Reject with validation error |
| Name too long | Name > 100 characters | Reject with validation error |
| No currency provided | Currency omitted | Default to "€" |
| Unverified user | User's email not verified | Reject with authorization error |

## Acceptance criteria

1. GIVEN a verified user with a valid name WHEN CreateGroup is executed THEN a `GroupCreated` event is emitted with currency="€" (default) followed by a `MemberJoined` event for the creator.
2. GIVEN a verified user with currency="🍺" WHEN CreateGroup is executed THEN `GroupCreated` carries currency="🍺".
3. GIVEN an empty group name WHEN CreateGroup is executed THEN it is rejected with a validation error.

## API contract

### POST /api/groups
Request:
```json
{
  "name": "Fantasy Liga 24/25",
  "currency": "🍺"
}
```
Response (201):
```json
{
  "groupId": "uuid",
  "name": "Fantasy Liga 24/25",
  "currency": "🍺"
}
```
Errors: 422 (validation error), 401 (not authenticated), 403 (email not verified)

### GET /api/groups
Response (200):
```json
[
  {
    "groupId": "uuid",
    "name": "Fantasy Liga 24/25",
    "currency": "🍺",
    "memberCount": 5,
    "createdAt": "2026-03-25T10:00:00Z"
  }
]
```

### GET /api/groups/{groupId}
Response (200):
```json
{
  "groupId": "uuid",
  "name": "Fantasy Liga 24/25",
  "currency": "🍺",
  "members": [
    { "userId": "uuid", "email": "user@example.com", "joinedAt": "..." }
  ],
  "createdAt": "2026-03-25T10:00:00Z"
}
```
Errors: 404 (group not found), 403 (not a member)

## Dependencies
- **Identity context** (upstream): user must be verified.

## Open questions
None.
