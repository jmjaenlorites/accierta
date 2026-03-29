---
name: inspect-db
description: How to inspect PostgreSQL database state including event store and projections. Use after making changes that write to the database — creating entities, placing bets, resolving porras, distributing points. Covers querying events, projections, and verifying data integrity via psql.
user-invocable: false
---

# Inspecting the Database

## Access

Use psql directly via Docker. **Always check your local ports first** — if you're in a worktree, ports may differ from defaults.

```bash
# Check your port (default 5432, worktrees use 5433, 5434, etc.)
grep -A2 'postgres' docker-compose.override.yml 2>/dev/null || echo "Using default ports"

# Interactive session (adjust port if in worktree)
docker compose exec postgres psql -U accierta -d accierta

# Run a single query
docker compose exec postgres psql -U accierta -d accierta -c "SELECT COUNT(*) FROM events;"

# Run a query from outside Docker (useful if container name conflicts)
psql -h localhost -p 5432 -U accierta -d accierta -c "SELECT COUNT(*) FROM events;"
```

## Key tables to inspect

### Event store

```sql
-- All events for a specific aggregate
SELECT event_type, payload, version, occurred_at
FROM events
WHERE aggregate_id = '<uuid>'
ORDER BY version;

-- Latest events of a specific type
SELECT aggregate_id, payload, occurred_at
FROM events
WHERE event_type = 'BetPlaced'
ORDER BY occurred_at DESC
LIMIT 10;

-- Count events by type
SELECT event_type, COUNT(*)
FROM events
GROUP BY event_type
ORDER BY count DESC;

-- Verify optimistic concurrency (no duplicate versions per aggregate)
SELECT aggregate_id, version, COUNT(*)
FROM events
GROUP BY aggregate_id, version
HAVING COUNT(*) > 1;
```

### Projections

Projection tables vary by bounded context. Common queries:

```sql
-- User projections
SELECT * FROM users WHERE email = 'test@example.com';

-- Group members
SELECT * FROM group_members WHERE group_id = '<uuid>';

-- Porra with bets
SELECT * FROM porras WHERE group_id = '<uuid>';
SELECT * FROM bets WHERE porra_id = '<uuid>';

-- Balances
SELECT * FROM balances WHERE group_id = '<uuid>' ORDER BY balance DESC;

-- Leaderboard stats
SELECT * FROM member_stats WHERE group_id = '<uuid>';
```

## What to verify after each operation

### After creating an entity (group, porra, user)
1. Event store has the creation event with correct payload
2. Projection table has the new row with correct fields
3. Version is 1 for new aggregates

### After a state change (bet placed, resolution proposed)
1. New event in event store with incremented version
2. Projection updated (e.g., balance decreased after bet)
3. Previous events still intact (events are immutable)

### After a cross-context operation (resolution → scoring)
1. Events emitted by source context exist
2. Projection in consumer context updated (e.g., balance changed after resolution)
3. No orphaned or missing events in the chain

## Data integrity checks

```sql
-- Verify all aggregate versions are sequential (no gaps)
SELECT aggregate_id, array_agg(version ORDER BY version) AS versions
FROM events
GROUP BY aggregate_id
HAVING array_agg(version ORDER BY version) != array_agg(DISTINCT version ORDER BY version);

-- Verify event payloads are valid JSON
SELECT id, event_type
FROM events
WHERE payload IS NULL OR payload::text = '';

-- Check for balance inconsistencies (balance should never be negative)
SELECT * FROM balances WHERE balance < 0;
```
