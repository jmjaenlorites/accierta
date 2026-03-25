# ADR-002: Hand-made Event Sourcing (no framework)

## Status
Accepted

## Context
Event sourcing is a project goal — both as an architectural pattern and as a learning exercise. Axon Framework is the most mature ES framework on the JVM, but it is invasive: aggregates require `@Aggregate`, `@CommandHandler`, and `@EventSourcingHandler` annotations, which contradicts our hexagonal architecture goal of a pure domain layer with zero framework dependencies.

## Decision
Implement event sourcing from scratch using pure Java domain + PostgreSQL event store.

How it works:
- Aggregates are pure Java classes. They receive commands, validate invariants, emit domain events, and reconstruct state by replaying events.
- The `EventStore` is a port (interface) defined by the domain. Infrastructure implements it with PostgreSQL (events table, append-only, JSON serialization).
- Projections (read models) are built by event handlers that subscribe to domain events.
- Optimistic concurrency is handled via an expected version on append.
- Snapshots and sagas can be added incrementally when needed.

## Consequences
- **Domain stays pure:** no framework annotations, no inheritance from framework classes. Hexagonal architecture is fully consistent.
- **More code to write:** the event store, event serialization, aggregate repository, and projection infrastructure are built manually (~200-400 lines of infrastructure code).
- **Deeper learning:** understanding ES internals (serialization, versioning, concurrency, replay) rather than learning a framework's API.
- **No free features:** Axon provides snapshotting, sagas, dead letter queue, and distributed event handling out of the box. We build these only when needed.
- **Simpler dependency tree:** no Axon Server or Axon dependencies.
