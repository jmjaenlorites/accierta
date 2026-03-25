# ADR-004: Hexagonal Architecture (Ports & Adapters)

## Status
Accepted

## Context
DDD places the domain model at the center of the design. We need an architectural pattern that protects the domain from infrastructure concerns and makes it testable in isolation.

Candidates: Hexagonal (Ports & Adapters), Clean Architecture, Onion Architecture, Vertical Slice.

## Decision
Hexagonal Architecture (Ports & Adapters) within each bounded context.

Structure per module:
- `domain/` — pure Java. Aggregates, value objects, domain events, domain services, and port interfaces. Zero external dependencies.
- `domain/port/in/` — inbound ports. Interfaces that the outside world uses to interact with the domain.
- `domain/port/out/` — outbound ports. Interfaces the domain needs from the outside (e.g., EventStore, repositories).
- `application/` — implements inbound ports. Orchestrates domain logic. May use Spring annotations.
- `infrastructure/` — implements outbound ports. Database access, event publishing, external services.
- `api/` — inbound adapters. REST controllers, WebSocket handlers, DTOs.

Dependency rule: `domain/` ← `application/` ← `infrastructure/` and `api/`. Dependencies always point inward. Enforced by ArchUnit.

Rejected alternatives:
- **Clean Architecture:** essentially the same as hexagonal but with more prescribed layers. Adds complexity without benefit at this scale.
- **Onion Architecture:** same concept, different naming. No practical difference.
- **Vertical Slice:** organizes by feature instead of by layer. Doesn't protect the domain layer — logic can leak into controllers. Poor fit for DDD.

## Consequences
- **Domain is fully testable without any framework.** Unit tests are pure Java, fast, and need no mocks (if the design is correct).
- **Infrastructure is swappable.** Changing from PostgreSQL to another store means implementing a new adapter — the domain doesn't change.
- **More files and packages** compared to a flat structure. Worth it for the clarity and protection it provides.
- **Requires discipline:** every developer (and AI agent) must respect the dependency rule. ArchUnit automates enforcement.
