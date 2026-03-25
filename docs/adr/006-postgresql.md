# ADR-006: PostgreSQL as single database

## Status
Accepted

## Context
We need a database for both the event store (append-only event log) and projections (read models). Options: PostgreSQL, MySQL, MongoDB, dedicated event store (EventStoreDB).

## Decision
PostgreSQL for everything — event store and projections.

- **Event store:** a single `events` table with append-only inserts, JSON serialized payloads, optimistic concurrency via aggregate version.
- **Projections:** standard relational tables optimized for read queries (leaderboard, porra details, history).
- **Both coexist in the same database** with separate schemas or table prefixes for clarity.

## Consequences
- **One database to operate.** Simpler deployment, backup, and monitoring.
- **PostgreSQL's JSONB** handles event payload storage efficiently with indexing support.
- **ACID transactions** for event appends (critical for optimistic concurrency).
- **Projections can use full SQL** — joins, aggregations, indexes. No ORM limitations.
- **No dedicated event store features** like built-in subscriptions or catch-up projections (EventStoreDB has these). We implement subscription/projection rebuild manually.
