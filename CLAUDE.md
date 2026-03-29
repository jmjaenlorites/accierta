# CLAUDE.md — Accierta

## Project overview

Accierta is a multiplayer betting app where friend groups create predictions on any event, bet fictional points, and compete on a leaderboard over time. Real money is managed outside the app by the group.

**PRD:** [PRD.md](./PRD.md) — conceptual starting point. Specs in `docs/specs/` are the source of truth for implementation.

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 21+ |
| Framework | Spring Boot 3.x + Spring Modulith |
| Event Sourcing / CQRS | Hand-made (pure Java domain + PostgreSQL event store) |
| Database | PostgreSQL |
| Real-time | Spring WebSocket (from day 1) |
| Auth | Hand-made email + password (no Clerk, no OAuth) |
| Email | Resend (transactional only: registration, reset, invitations) |
| Testing | JUnit 5 + Mockito + Testcontainers + ArchUnit |
| Build | Gradle (Kotlin DSL) |
| Frontend | React (PWA) + Tailwind CSS + Lucide icons |
| Infrastructure | Docker Compose (all services) |
| MCP Servers | Chrome (PWA testing) |

## Educational context

The developer is experienced with Node.js and Python but is new to:
- **Spring Boot** — explain Spring concepts (dependency injection, annotations, auto-configuration, beans, profiles) when introducing them
- **Gradle** — explain build concepts (tasks, plugins, dependency configurations) when they come up
- **DDD** — explain patterns (aggregates, value objects, domain events, repositories, bounded contexts) with concrete examples from this project
- **Event sourcing** — explain the concepts (event store, aggregate reconstruction, projections, snapshots) with concrete examples. We implement ES from scratch, not via a framework.

When writing code or explaining architecture, provide brief "why" context for Spring/DDD/ES patterns that wouldn't be obvious to someone coming from Express.js or FastAPI.

---

## Development methodology: Spec-Driven Development (SDD)

### Principle

> The spec is the source of truth. Code implements the spec. Tests verify the spec. Documentation is the spec.

Every feature starts as a spec. No implementation without a spec. No test without a spec to derive it from.

### Workflow

```
1. Write spec (docs/specs/<context>/<feature>.md)
2. Review spec — all edge cases, invariants, and acceptance criteria defined
3. Derive test cases from spec acceptance criteria
4. TDD: write failing tests → implement → green → refactor
5. Manual verification — test as a real user would (see skills below)
6. Verify implementation matches spec
```

**A feature is NOT done until step 5 passes.** Automated tests are necessary but not sufficient.

### Spec format

Every spec follows this structure. All sections are mandatory — if a section doesn't apply, write "N/A" with a reason.

```markdown
# <Feature name>

## Context
Which bounded context this belongs to and why.

## Description
What this feature does in 2-3 sentences. Written from the user's perspective.

## Domain model

### Entities
- Entity name — what it represents, its lifecycle

### Value objects
- VO name — what it encapsulates, validation rules

### Domain events
- EventName — when it is emitted, what it carries

## Rules and invariants
Numbered list of business rules that must ALWAYS hold true.
These become assertions in tests.

1. Rule description
2. Rule description

## Commands and queries

### Commands (write operations)
| Command | Input | Output | Rules applied |
|---------|-------|--------|---------------|
| CommandName | fields | success/failure | rule numbers |

### Queries (read operations)
| Query | Input | Output | Notes |
|-------|-------|--------|-------|
| QueryName | filters | projection shape | |

## Behavior

### Happy path
Step-by-step flow for the main success scenario.

### Edge cases
| Case | Trigger | Expected behavior |
|------|---------|-------------------|
| Description | What causes it | What should happen |

## Acceptance criteria
Numbered list. Each one maps to one or more tests.

1. GIVEN ... WHEN ... THEN ...
2. GIVEN ... WHEN ... THEN ...

## API contract
HTTP endpoints for this feature (method, path, request/response shapes).
These are informational — the spec rules take precedence over any implementation detail.

## Dependencies
Which other specs/contexts this feature depends on or is depended upon by.

## Open questions
Anything unresolved. Must be empty before implementation starts.
```

---

## Architecture: DDD + Hexagonal Architecture (Ports & Adapters)

### Core principle

The domain is at the center and has ZERO external dependencies. It communicates with the outside world through **ports** (interfaces defined by the domain) and **adapters** (implementations provided by infrastructure).

```
HTTP/WS request → [Adapter in] → [Port in] → Domain logic → [Port out] → [Adapter out] → DB/Events
```

**Dependency rule:** dependencies always point inward. `domain/` imports nothing from `application/`, `infrastructure/`, or `api/`. This is enforced by ArchUnit tests.

### Bounded contexts

| Context | Responsibility |
|---------|---------------|
| **Identity** | User registration, authentication, magic link, sessions |
| **Groups** | Group creation, invitation, membership, leaving |
| **Betting** | Porra creation, bet placement, porra lifecycle |
| **Resolution** | Proposing results, disputes, voting, consensus |
| **Settlement** | Debt generation, tracking, payment confirmation, leaderboard, stats |

### DDD conventions

- **Aggregates** are the consistency boundary. All state changes go through the aggregate root.
- **Value objects** are immutable. Equality is structural (by value, not by reference).
- **Domain events** are the facts of what happened. They are immutable and past-tense named (`BetPlaced`, not `PlaceBet`).
- **Commands** are the intent to change state. Present-tense imperative (`PlaceBet`).
- **Repositories** only exist for aggregate roots. The domain defines the repository interface (port out); infrastructure provides the implementation (adapter out).
- **Domain services** contain logic that doesn't belong to a single aggregate.
- **No framework annotations in the domain layer.** No Spring, no Axon, no JPA annotations in `domain/`. The domain is pure Java.

### Module structure (Spring Modulith + Hexagonal)

```
src/main/java/com/accierta/
  identity/                        # Bounded context: Identity
    domain/                        #   The hexagon — pure Java, zero dependencies
      model/                       #     Aggregates, entities, value objects
      event/                       #     Domain events
      service/                     #     Domain services
      port/
        in/                        #     Inbound ports — interfaces the outside calls into the domain
        out/                       #     Outbound ports — interfaces the domain needs from the outside
    application/                   #   Orchestration layer — implements inbound ports
      handler/                     #     Command and query handlers
    infrastructure/                #   Outbound adapters — implements outbound ports
      persistence/                 #     Repository implementations (event store, JPA projections)
      messaging/                   #     Event publishing to other contexts
    api/                           #   Inbound adapters — translate external input to commands/queries
      rest/                        #     REST controllers
      websocket/                   #     WebSocket handlers
      dto/                         #     Request/response objects and mappers
  groups/                          # Same internal structure as identity
  betting/                         # Same internal structure
  resolution/                      # Same internal structure
  settlement/                      # Same internal structure
  shared/                          # Shared kernel (only truly shared value objects/events)
```

Each bounded context is a Spring Modulith module. Inter-module communication is through **domain events only** — no direct method calls between modules. ArchUnit tests enforce:
1. `domain/` has no imports from `application/`, `infrastructure/`, `api/`, or any framework
2. `application/` does not import from `infrastructure/` or `api/`
3. No cross-module imports (e.g., `betting/` cannot import from `resolution/`)

### Event sourcing (hand-made)

Event sourcing is implemented from scratch — no Axon or external ES framework. This keeps the domain pure and maximizes learning.

**Core concepts:**
- **Aggregates** receive commands, validate invariants, and emit domain events. They reconstruct their state by replaying their event history. Aggregates are pure Java — no annotations, no framework inheritance.
- **Event store** is a PostgreSQL table that stores events as immutable, append-only records. The domain defines an `EventStore` port (interface); infrastructure implements it with JDBC/JPA.
- **Projections** (read models) are built by event handlers that subscribe to domain events. They are optimized for queries and are fully rebuildable from the event store.
- **Snapshots** can be added later as an optimization — store the aggregate state at a point in time to avoid replaying the full event history.
- **Sagas** coordinate cross-aggregate workflows (e.g., resolution → scoring). Implemented as event-driven state machines in the application layer.

**How an aggregate works:**

```java
// domain/model/Porra.java — pure Java, no annotations
public class Porra {
    private PorraId id;
    private PorraStatus status;
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    // Reconstruct from event history
    public static Porra reconstitute(List<DomainEvent> history) {
        Porra porra = new Porra();
        history.forEach(porra::apply);
        return porra;
    }

    // Command: validate invariants, emit event
    public void placeBet(UserId userId, BetAmount amount) {
        if (status != PorraStatus.OPEN) throw new PorraNotOpenException(id);
        // ... more invariant checks
        emit(new BetPlaced(id, userId, amount, Instant.now()));
    }

    // Apply event to update state
    private void apply(DomainEvent event) {
        if (event instanceof BetPlaced e) {
            // update internal state
        }
    }

    private void emit(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }
}
```

**Event store port (defined by the domain):**

```java
// domain/port/out/EventStore.java
public interface EventStore {
    void append(AggregateId id, List<DomainEvent> events, long expectedVersion);
    List<DomainEvent> load(AggregateId id);
}
```

**Infrastructure implements it with PostgreSQL:**

```java
// infrastructure/persistence/PostgresEventStore.java
@Repository
public class PostgresEventStore implements EventStore {
    // Serializes events to JSON, stores in events table
    // Handles optimistic concurrency via expectedVersion
}
```

---

## Testing strategy

### Test pyramid

| Level | What | Tool | Derived from |
|-------|------|------|-------------|
| **Unit (domain)** | Aggregates, value objects, domain services — pure Java, no framework | JUnit 5 | Spec rules and invariants |
| **Unit (application)** | Command/query handlers with mocked outbound ports | JUnit 5 + Mockito | Spec commands and queries |
| **Integration** | Full module behavior (adapter in → domain → adapter out) | JUnit 5 + Testcontainers | Spec acceptance criteria |
| **Architecture** | Hexagonal dependency rules, module boundaries | ArchUnit | Architecture conventions |
| **Contract** | API endpoints match spec | Spring MockMvc | Spec API contract |

### Testing conventions

- Every acceptance criterion in a spec maps to at least one test
- Test names follow: `should_<expected>_when_<condition>` (e.g., `should_reject_bet_when_deadline_passed`)
- **Domain tests are pure Java** — no Spring context, no mocks, no framework. If a domain test needs a mock, the design is wrong (logic leaked outside the domain).
- Application tests mock **outbound ports only** (the interfaces the domain defines). Never mock domain objects.
- Integration tests use Testcontainers for a real PostgreSQL instance
- Tests mirror the module and layer structure:
  ```
  src/test/java/com/accierta/
    betting/
      domain/          # Pure unit tests — fast, no dependencies
      application/     # Handler tests with mocked ports
      infrastructure/  # Adapter tests with Testcontainers
      api/             # Controller contract tests with MockMvc
  ```

---

## Documentation hierarchy

```
CLAUDE.md                              # This file — entry point for AI agents and developers
PRD.md                                 # Conceptual starting point (not source of truth for implementation)
docs/
  architecture.md                      # System architecture overview, bounded context map, data flow
  design-system.md                     # UI/UX design guide — colors, typography, components, animations
  specs/
    identity/                          # Specs for Identity context
    groups/                            # Specs for Groups context
    betting/                           # Specs for Betting context
    resolution/                        # Specs for Resolution context
    settlement/                        # Specs for Settlement context
  adr/                                 # Architecture Decision Records
    001-java-spring-boot.md
    002-event-sourcing-handmade.md
    003-monolith-modular.md
    ...
```

### ADR format

```markdown
# ADR-NNN: Title

## Status
Accepted | Superseded | Deprecated

## Context
What is the issue that we're seeing that is motivating this decision?

## Decision
What is the change that we're proposing and/or doing?

## Consequences
What becomes easier or harder as a result of this decision?
```

---

## Agent skills (manual verification & tooling)

Skills are located in `.claude/skills/`. They provide instructions for tasks that go beyond writing code and tests.

**Read the `manual-verification` skill before closing any feature.** Manual testing is mandatory.

| Skill | When to use |
|-------|------------|
| `manual-verification` | **Mandatory** before marking any feature as done. Checklist covering all layers. |
| `test-api` | After implementing or modifying REST endpoints. Test with curl, verify responses and errors. |
| `inspect-db` | After any write operation. Verify events in event store, projections, data integrity. |
| `test-pwa` | After implementing or modifying UI. Validate in a real browser via Chrome MCP. |
| `docker-env` | Starting/stopping services, resetting database, viewing logs. |

### Infrastructure: Docker Compose

All services run in Docker. Never install PostgreSQL, Node, or Java locally.

```bash
docker compose up -d          # Start everything
docker compose down -v        # Full reset (wipe database)
docker compose logs -f app    # View backend logs
```

### MCP servers

- **Chrome MCP**: interact with the PWA in a real browser. Navigate, click, fill forms, take screenshots. Use for validating UI changes.

### Parallel agents (worktrees)

Multiple agents may work in parallel on different features using git worktrees. Each agent gets an isolated environment:

- **Each worktree has its own `docker-compose.override.yml`** with unique ports to avoid collisions.
- **Each agent uses its own database** — no shared state between agents.
- Port allocation convention:
  - Main: app=8080, frontend=3000, postgres=5432
  - Worktree 1: app=8081, frontend=3001, postgres=5433
  - Worktree 2: app=8082, frontend=3002, postgres=5434
  - Pattern: base_port + worktree_index
- The `docker-env` skill explains how to set this up.
- **Never assume ports.** Always read the local `docker-compose.override.yml` (or env vars) to know which ports your instance uses.

---

## Code conventions

- Follow standard Java conventions (Google Java Style Guide)
- No abbreviations in names — `resolution`, not `res`
- Domain language in code must match the ubiquitous language defined in specs
- All public methods have clear intent — no generic names like `process()` or `handle()`
- Prefer small, focused classes over large ones
- No utility classes with static methods in the domain layer

## Git conventions

- Commits are atomic — one logical change per commit
- Commit messages: `<context>: <what and why>` (e.g., `betting: add deadline validation to PlaceBet command`)
- Feature branches: `feature/<context>/<short-description>`
- All code on a branch must have passing tests before merge
- **Parallel work uses git worktrees.** When multiple agents work simultaneously, each agent operates in its own worktree with an isolated Docker environment (unique ports, separate database). See the `docker-env` skill for setup details. Never work directly on `main` — always use a feature branch in a worktree.
