# Dispute and Vote

## Context
Resolution bounded context. Disputes are the mechanism to challenge a proposed resolution and trigger a group vote.

## Description
During the 48-hour waiting period, any bettor (except the proposer) can dispute the proposed resolution. A dispute triggers a voting phase where bettors (excluding the proposer and the disputer) vote on the correct result. The majority wins. In case of a tie, a random voter's choice is selected.

## Domain model

### Entities
- **Resolution** — existing aggregate. Dispute adds state: disputed, votes, final result.

### Value objects
- **Dispute** — contains: disputerId, reason (optional), disputedAt.
- **Vote** — contains: voterId, votedResult, votedAt.
- **VotingPeriod** — 48 hours from dispute time.

### Domain events
- `ResolutionDisputed` — emitted when someone disputes. Carries: resolutionId, porraId, disputerId, reason?, occurredAt.
- `VoteCast` — emitted when a voter submits their vote. Carries: resolutionId, voterId, votedResult, occurredAt.
- `ResolutionAccepted` — emitted when voting concludes with a result. Carries: resolutionId, porraId, acceptedResult, method (CONSENSUS | VOTE | TIEBREAK), occurredAt.

## Rules and invariants

1. Only bettors can dispute (not the proposer, not non-bettors).
3. Only one dispute is allowed per proposal. Once disputed, voting begins.
4. The disputer cannot vote.
5. The proposer cannot vote.
6. Only bettors (excluding proposer and disputer) can vote.
7. Each voter votes exactly once. No changes.
8. A vote must be a valid result for the porra type (same validation as proposal).
9. For MULTIPLE_CHOICE: voters can also vote "NONE_OF_THE_ABOVE".
10. Voting has a 48-hour window. After that, votes are tallied.
11. The result with the most votes wins.
12. If there's a tie: one random voter among those who voted for the tied options is selected, and their vote decides.
13. If no one votes within 48h: the original proposal stands (auto-accepted).
14. After the result is determined, Scoring distributes points.

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| DisputeResolution | resolutionId, disputerId, reason? | void | 1, 2, 3 |
| CastVote | resolutionId, voterId, votedResult | void | 4, 5, 6, 7, 8, 9 |
| TallyVotes | resolutionId | void | 10, 11, 12, 13 |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| GetVotingStatus | resolutionId | votes cast, eligible voters, time remaining | Voting progress view |
| GetVoteResults | resolutionId | tally per option, winner, method | After voting closes |

## Behavior

### Happy path (dispute → vote → majority)
1. Bettor disputes the proposed resolution during the 48h window.
2. System emits `ResolutionDisputed`. Waiting period cancelled, voting begins.
3. Eligible voters (bettors minus proposer and disputer) cast votes.
4. Each vote emits `VoteCast`.
5. After 48h, scheduled job runs TallyVotes.
6. Majority option wins → `ResolutionAccepted` emitted with method=VOTE.
7. Scoring distributes points.

### Happy path (tie → random tiebreak)
1-4. Same as above.
5. After 48h, TallyVotes finds a tie (e.g., 2 votes for A, 2 votes for B).
6. System randomly selects one voter from those who voted for a tied option.
7. That voter's choice becomes the result → `ResolutionAccepted` emitted with method=TIEBREAK.

### Happy path (no votes after dispute)
1-2. Same as above.
5. After 48h, no votes cast.
6. Original proposal stands → `ResolutionAccepted` emitted with the original proposed result, method=CONSENSUS.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Proposer tries to dispute | Proposer disputes their own proposal | Reject — proposer cannot dispute |
| Non-bettor disputes | Group member who didn't bet | Reject — only bettors can dispute |
| Proposer tries to vote | Proposer submits a vote | Reject — proposer cannot vote |
| Disputer tries to vote | Disputer submits a vote | Reject — disputer cannot vote |
| Duplicate vote | Voter votes again | Reject — already voted |
| Vote after 48h | Vote submitted after voting window | Reject — voting closed |
| Only 1 eligible voter | 3 bettors: proposer, disputer, 1 other | That one voter's choice is the result |
| 0 eligible voters | 2 bettors: proposer and disputer | No votes possible → original proposal stands |
| All voted same thing | Everyone votes the same result | That result wins with method=VOTE |

## Acceptance criteria

1. GIVEN an active proposal WHEN a bettor (not proposer) disputes THEN `ResolutionDisputed` is emitted and voting begins.
2. GIVEN a dispute WHEN the proposer tries to vote THEN it is rejected.
3. GIVEN a dispute WHEN the disputer tries to vote THEN it is rejected.
4. GIVEN voting is open WHEN an eligible voter casts a vote THEN `VoteCast` is emitted.
5. GIVEN a voter who already voted WHEN they try to vote again THEN it is rejected.
6. GIVEN voting ended with a clear majority WHEN TallyVotes runs THEN `ResolutionAccepted` is emitted with the majority result and method=VOTE.
7. GIVEN voting ended in a tie WHEN TallyVotes runs THEN a random voter among tied options is selected and `ResolutionAccepted` is emitted with method=TIEBREAK.
8. GIVEN voting ended with no votes cast WHEN TallyVotes runs THEN `ResolutionAccepted` is emitted with the original proposed result and method=CONSENSUS.
9. GIVEN only 2 bettors (proposer and disputer) WHEN TallyVotes runs THEN the original proposal stands (0 eligible voters = no votes possible).

## API contract

### POST /api/porras/{porraId}/resolution/dispute
Request:
```json
{
  "reason": "El resultado fue diferente, vi el partido"
}
```
Response (201):
```json
{
  "message": "Resolution disputed, voting is now open",
  "votingEndsAt": "2026-03-29T14:30:00Z"
}
```
Errors: 403 (not a bettor, is proposer), 400 (no active proposal, already disputed)

### POST /api/porras/{porraId}/resolution/vote
Request:
```json
{
  "votedResult": "Real Madrid"
}
```
Response (201):
```json
{
  "message": "Vote cast"
}
```
Errors: 403 (not eligible — proposer, disputer, non-bettor), 400 (already voted, voting closed)

### GET /api/porras/{porraId}/resolution/votes
Response (200):
```json
{
  "status": "VOTING",
  "votingEndsAt": "2026-03-29T14:30:00Z",
  "eligibleVoters": 4,
  "votesCast": 2,
  "votes": [
    { "voterId": "uuid", "votedResult": "Real Madrid" },
    { "voterId": "uuid", "votedResult": "Barcelona" }
  ]
}
```

### WebSocket: /porra/{porraId}
On `ResolutionDisputed`:
```json
{
  "type": "RESOLUTION_DISPUTED",
  "disputedBy": "uuid",
  "reason": "El resultado fue diferente",
  "votingEndsAt": "2026-03-29T14:30:00Z"
}
```
On `VoteCast`:
```json
{
  "type": "VOTE_CAST",
  "voterId": "uuid",
  "votesCast": 3,
  "eligibleVoters": 5
}
```
On `ResolutionAccepted`:
```json
{
  "type": "RESOLUTION_ACCEPTED",
  "acceptedResult": "Barcelona",
  "method": "VOTE"
}
```

## Dependencies
- **Propose resolution spec** (same context): dispute follows a proposal.
- **Betting context** (upstream): who bet, porra type and options.
- **Settlement context** (downstream): `ResolutionAccepted` triggers debt generation.
- **Scheduled infrastructure**: 48h vote tally job.

## Open questions
None.
