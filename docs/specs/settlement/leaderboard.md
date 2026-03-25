# Leaderboard and Stats

## Context
Settlement bounded context. The leaderboard is the competitive heart of the app — it shows who's the best predictor and who's winning the most.

## Description
Each group has a real-time leaderboard with multiple rankings. Members can be ranked by porras won, money earned, win ratio, and money ratio. Each member has a profile with detailed stats. The leaderboard updates in real-time via WebSocket.

## Domain model

### Entities
N/A — leaderboard and stats are read-only projections built from events. They are not aggregates.

### Value objects
- **MemberStats** — projection containing:
  - totalPorras: number of porras participated in
  - won: porras won
  - lost: porras lost
  - cancelled: porras cancelled
  - winRatio: won / (won + lost) — cancelled excluded
  - currentStreak: consecutive wins (resets on loss, cancelled doesn't affect)
  - totalMoneyWon: sum of all money won
  - totalMoneyLost: sum of all money lost (stakes on lost porras)
  - netMoney: totalMoneyWon - totalMoneyLost
  - moneyRatio: totalMoneyWon / (totalMoneyWon + totalMoneyLost)
  - pendingDebtsOwed: total money others owe this member (unsettled)
  - pendingDebtsOwing: total money this member owes others (unsettled)

### Domain events
N/A — this spec defines projections that consume events from other specs. No new events.

## Rules and invariants

1. Leaderboard supports 4 ranking modes:
   - **By porras won** — absolute count, descending
   - **By win ratio** — won/(won+lost), descending. Minimum 3 porras to appear in this ranking (avoids 1/1 = 100%).
   - **By net money** — totalMoneyWon - totalMoneyLost, descending
   - **By money ratio** — totalMoneyWon/(totalMoneyWon+totalMoneyLost), descending. Minimum 3 porras.
2. Cancelled porras are excluded from ratios but shown in stats.
3. Current streak counts consecutive wins. Resets to 0 on a loss. Cancelled porras don't affect the streak.
4. A member who hasn't participated in any porra has stats of 0 across the board.
5. Leaderboard includes all group members, even those with no participation.
6. Money amounts use the group's configured currency.

## Commands and queries

### Commands (write operations)
N/A — projections are updated reactively from events.

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetLeaderboard | groupId, rankBy | ranked list of (member, stats) | Sorted by chosen ranking mode |
| GetMemberStats | groupId, memberId | full MemberStats | Individual profile |
| GetMemberHistory | groupId, memberId, pagination | list of past porras with result and bet | Chronological |

## Behavior

### Projection updates
The leaderboard projection is rebuilt from these events:
- `DebtGenerated` → update money stats (won/lost), update porras won/lost counts
- `PorraCancelled` / `PorraAutoCancelled` → increment cancelled count
- `DebtSettled` → update pending debts totals
- `MemberJoined` → add member to leaderboard with zero stats
- `MemberLeft` → mark member as "left" (stats remain for history integrity)

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Tie in ranking | Two members same value | Sort by secondary criteria: for porras, use win ratio; for money, use net money; then by earliest join date |
| No porras yet | Group just created | Leaderboard shows all members with 0 stats |
| Fewer than 3 porras | Member only participated in 1 porra | Appears in "by porras won" and "by net money" rankings, but NOT in ratio rankings |
| Member left | Left the group | Still appears in leaderboard marked as "left", stats frozen |

## Acceptance criteria

1. GIVEN a group with members having different win counts WHEN GetLeaderboard(rankBy=PORRAS_WON) is queried THEN members are ranked by porras won descending.
2. GIVEN a member who won 5 of 8 porras (2 cancelled), earning 50€ and losing 30€ WHEN GetMemberStats is queried THEN winRatio=5/6 (83.3%), netMoney=20€, moneyRatio=50/80 (62.5%), cancelled=2.
3. GIVEN a member with only 2 porras WHEN GetLeaderboard(rankBy=WIN_RATIO) is queried THEN the member does NOT appear (minimum 3 porras for ratio rankings).
4. GIVEN a member who won 3 in a row, had 1 cancelled, then won 1 WHEN GetMemberStats is queried THEN currentStreak=4.
5. GIVEN a porra resolution WHEN debts are generated THEN the leaderboard is updated and a WebSocket message is sent to `/group/{id}/leaderboard`.
6. GIVEN a group with currency "🍺" WHEN GetLeaderboard is queried THEN all money amounts show the beer emoji as currency.

## API contract

### GET /api/groups/{groupId}/leaderboard?rankBy=NET_MONEY
Query params: `rankBy` = PORRAS_WON | WIN_RATIO | NET_MONEY | MONEY_RATIO (default: NET_MONEY)

Response (200):
```json
{
  "rankBy": "NET_MONEY",
  "currency": "€",
  "rankings": [
    {
      "rank": 1,
      "userId": "uuid",
      "email": "alice@example.com",
      "left": false,
      "stats": {
        "totalPorras": 10,
        "won": 7,
        "lost": 2,
        "cancelled": 1,
        "winRatio": 0.778,
        "currentStreak": 3,
        "totalMoneyWon": 85.00,
        "totalMoneyLost": 30.00,
        "netMoney": 55.00,
        "moneyRatio": 0.739,
        "pendingDebtsOwed": 20.00,
        "pendingDebtsOwing": 5.00
      }
    }
  ]
}
```
Errors: 403 (not a member)

### GET /api/groups/{groupId}/members/{userId}/stats
Response (200):
```json
{
  "totalPorras": 10,
  "won": 7,
  "lost": 2,
  "cancelled": 1,
  "winRatio": 0.778,
  "currentStreak": 3,
  "totalMoneyWon": 85.00,
  "totalMoneyLost": 30.00,
  "netMoney": 55.00,
  "moneyRatio": 0.739,
  "pendingDebtsOwed": 20.00,
  "pendingDebtsOwing": 5.00,
  "currency": "€"
}
```

### GET /api/groups/{groupId}/members/{userId}/history?page=0&size=20
Response (200):
```json
{
  "content": [
    {
      "porraId": "uuid",
      "title": "¿Quién gana la liga?",
      "type": "MULTIPLE_CHOICE",
      "myBet": "Real Madrid",
      "result": "Barcelona",
      "outcome": "LOST",
      "moneyChange": -10.00,
      "currency": "€",
      "resolvedAt": "2026-03-20T15:00:00Z"
    }
  ],
  "page": 0,
  "totalPages": 3
}
```

### WebSocket: /group/{groupId}/leaderboard
Pushes updated leaderboard on every resolution.

## Dependencies
- **Debt management spec** (same context): consumes DebtGenerated, DebtSettled events.
- **Betting context** (upstream): PorraAutoCancelled.
- **Resolution context** (upstream): PorraCancelled.
- **Groups context** (upstream): MemberJoined, MemberLeft, group currency.
- **WebSocket** (infrastructure): for real-time leaderboard updates.

## Open questions
None.
