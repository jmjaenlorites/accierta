# Numeric Resolution

## Context
Resolution bounded context. Special rules for resolving NUMERIC type porras, which have tolerance-based winner determination.

## Description
When a NUMERIC porra is resolved, the system determines winners based on proximity to the actual result and the tolerance margin. This spec extends the general resolution flow with the specific logic for numeric porras.

## Domain model

### Entities
- Uses the existing Resolution aggregate. This spec adds winner determination logic.

### Value objects
- **ToleranceMargin** — the absolute range around the actual result within which a bet is considered "close enough". Calculated from the median of all bet distances to the actual result, scaled by the tolerance percentage.
- **NumericResult** — BigDecimal, the actual real-world outcome.

### Domain events
- Uses `ResolutionAccepted` from the general resolution flow. The Scoring context uses this event plus the bets to determine winners.

## Rules and invariants

1. The winner is the bettor closest to the actual result AND whose bet is within the tolerance margin.
2. Tolerance margin calculation (always the same formula):
   - Compute the absolute distance from each bet to the actual result.
   - Take the **median** of those distances.
   - margin = median * tolerancePercentage.
   - Range: [actualResult - margin, actualResult + margin].
   - This formula adapts to the spread of bets. If everyone is close, the margin is tight. If bets are spread out, the margin is wider. Works for any result value including 0.
3. If no bettor is within the tolerance margin, nobody wins — all bets are refunded.
4. If multiple bettors are equally close (exact same distance to result), the pot is split equally.
5. Bets are BigDecimal — distance is calculated as absolute value of (bet - actualResult).
6. A bettor outside the margin never wins, even if they are the closest.
7. Median of an even number of distances: average of the two middle values. Median of a single distance: that distance itself.

## Commands and queries

### Commands (write operations)
N/A — this spec defines the winner determination logic used by the Scoring context when processing a `ResolutionAccepted` event for a NUMERIC porra.

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| DetermineNumericWinners | actualResult, tolerance, bets | list of winners (or empty) | Called by Scoring on resolution |

## Behavior

### Happy path (one clear winner)
1. Porra resolved with actualResult=100, tolerance=0.15.
2. Bets: Alice=95, Bob=120, Carol=80.
3. Distances: 5, 20, 20. Median = 20.
4. Margin = 20 * 0.15 = 3. Range: [97, 103].
5. Alice (distance 5) is outside. Bob (distance 20) is outside. Carol (distance 20) is outside.
6. Nobody within range → all bets refunded.

### Happy path (tight group of bets)
1. actualResult=100, tolerance=0.15.
2. Bets: Alice=98, Bob=104, Carol=110.
3. Distances: 2, 4, 10. Median = 4.
4. Margin = 4 * 0.15 = 0.6. Range: [99.4, 100.6].
5. Nobody within range → refund.

### Happy path (wider spread, winner emerges)
1. actualResult=100, tolerance=0.15.
2. Bets: Alice=99, Bob=130, Carol=200.
3. Distances: 1, 30, 100. Median = 30.
4. Margin = 30 * 0.15 = 4.5. Range: [95.5, 104.5].
5. Alice (distance 1) is within range → Alice wins.

### Happy path (multiple winners, split)
1. actualResult=100, tolerance=0.15.
2. Bets: Alice=99, Bob=101, Carol=200.
3. Distances: 1, 1, 100. Median = 1.
4. Margin = 1 * 0.15 = 0.15. Range: [99.85, 100.15].
5. Nobody within range → refund. (Even though Alice and Bob are very close, the median is small because most bets are close.)

### Happy path (split with larger spread)
1. actualResult=100, tolerance=0.15.
2. Bets: Alice=99, Bob=101, Carol=200, Dave=150.
3. Distances: 1, 1, 100, 50. Median = (1+50)/2 = 25.5.
4. Margin = 25.5 * 0.15 = 3.825. Range: [96.175, 103.825].
5. Alice (distance 1) and Bob (distance 1) within range. Equal distance → pot splits 50/50.

### Happy path (result is 0)
1. actualResult=0, tolerance=0.15.
2. Bets: Alice=1, Bob=10, Carol=20.
3. Distances: 1, 10, 20. Median = 10.
4. Margin = 10 * 0.15 = 1.5. Range: [-1.5, 1.5].
5. Alice (distance 1) is within range → Alice wins.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Nobody within margin | All bets far from result | No winner, all bets refunded |
| Exact match | Someone bet exactly the result | They win (distance 0, within any margin) |
| Negative result | Result is -5 (if applicable) | Median-based margin works the same — distances are absolute values |
| Very small result | Result is 0.001 | Median-based margin adapts to bet spread, not the result value |
| Single bettor | Only one bet | Cannot happen — porra-lifecycle auto-cancels porras with fewer than 2 bets at deadline |
| Two bettors | Distances d1, d2 | Median = (d1+d2)/2 |
| Single bettor, within margin | Only one person bet | They win the pot (which is just their own stake — net zero unless others didn't bet) |
| All bettors equidistant | Everyone bet 90 for result 100 | All win, pot split equally (everyone gets their stake back) |
| Odd number of winners in split | 3 winners, pot=100 | 33+33+34 — remainder to random winner (see balance-management spec) |

## Acceptance criteria

1. GIVEN actualResult=100, tolerance=0.15, bets=[99, 130, 200] WHEN DetermineNumericWinners is called THEN distances=[1,30,100], median=30, margin=4.5, range=[95.5,104.5], bettor with 99 wins.
2. GIVEN actualResult=100, tolerance=0.15, bets=[99, 101, 200, 150] WHEN DetermineNumericWinners is called THEN distances=[1,1,100,50], median=25.5, margin=3.825, bettors 99 and 101 both win (split).
3. GIVEN actualResult=100, tolerance=0.15, bets=[200, 300] WHEN DetermineNumericWinners is called THEN distances=[100,200], median=150, margin=22.5, range=[77.5,122.5], no winners — refund.
4. GIVEN actualResult=0, tolerance=0.15, bets=[1, 10, 20] WHEN DetermineNumericWinners is called THEN distances=[1,10,20], median=10, margin=1.5, bettor with 1 wins.
5. GIVEN actualResult=0, tolerance=0.15, bets=[5, 10, 20] WHEN DetermineNumericWinners is called THEN distances=[5,10,20], median=10, margin=1.5, no winners — refund.
6. GIVEN a single bettor WHEN the betting deadline passes THEN the porra is auto-cancelled before resolution (see porra-lifecycle spec). This case never reaches DetermineNumericWinners.
7. GIVEN actualResult=100, tolerance=0.15, bets=[90, 90, 200] WHEN DetermineNumericWinners is called THEN distances=[10,10,100], median=10, margin=1.5, no winners — refund.

## API contract

N/A — this logic is internal to the Scoring context's event handler for `ResolutionAccepted`. The resolution API endpoints from propose-resolution and dispute-and-vote specs are used as-is.

## Dependencies
- **Propose resolution spec** (same context): the actual result comes from the accepted resolution.
- **Scoring context** (downstream): uses this logic to determine winners and distribute points.
- **Balance management spec** (Scoring): handles the actual pot split, including remainder distribution.

## Open questions
None.
