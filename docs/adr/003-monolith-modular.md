# ADR-003: Modular Monolith (no microservices)

## Status
Accepted

## Context
The application has 5 bounded contexts (Identity, Groups, Betting, Resolution, Scoring). We need to decide how to deploy them: as a single monolith, a modular monolith, or separate microservices.

## Decision
Modular monolith using Spring Modulith.

Each bounded context is a top-level Java package that Spring Modulith treats as a module. Modules communicate exclusively through domain events — no direct method calls or shared database tables between modules.

## Consequences
- **Simpler deployment:** one application, one database, one deploy pipeline.
- **Enforced boundaries:** Spring Modulith + ArchUnit tests verify that modules don't depend on each other directly. If we ever need to extract a microservice, the boundaries are already clean.
- **Single transaction boundary:** within a module, we get ACID transactions. Cross-module communication is eventually consistent via events.
- **No network overhead:** inter-module events are in-process. No serialization, no retries, no service discovery.
- **Scales far enough:** for the expected user base (small friend groups), a single instance with PostgreSQL is more than sufficient.
