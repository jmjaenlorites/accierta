# ADR-001: Java 21+ with Spring Boot 3.x

## Status
Accepted

## Context
We need a language and framework for a DDD + Event Sourcing + Hexagonal Architecture project. The developer works daily with Node.js and Python and wants to explore something different. Candidates evaluated: Go, Java, Kotlin, Rust.

## Decision
Java 21+ with Spring Boot 3.x and Spring Modulith.

Reasons:
- **DDD support:** Java 21+ has records (value objects), sealed classes (sum types), and pattern matching. The JVM ecosystem has the most mature DDD community, books, and reference architectures.
- **Spring Modulith:** enforces bounded context boundaries at the module level, with tooling for verifying module dependencies.
- **Type system:** strong enough to model domain invariants. Records give structural equality for value objects. Sealed classes model domain states exhaustively.
- **VS Code support:** the Extension Pack for Java (Microsoft) provides solid support. Kotlin's VS Code experience is weaker.
- **Learning value:** different enough from Node.js/Python to be educational, without being so alien (like Rust) that the language itself becomes the focus instead of DDD/ES.

Rejected alternatives:
- **Kotlin:** developer doesn't know it and prefers not to learn it for this project. Same JVM benefits but adds a language learning curve.
- **Go:** weak type system for DDD (no sum types, no null safety, no enums). Would spend time fighting the language instead of learning DDD.
- **Rust:** ownership model creates friction with DDD aggregate patterns. ES ecosystem is immature. Learning curve (3-6 months) would dominate the project.

## Consequences
- Need to learn Spring Boot concepts (DI, annotations, auto-configuration, beans, profiles).
- Need to learn Gradle build system.
- JVM startup time is slower than Go/Node — acceptable for this project.
- Rich testing ecosystem (JUnit 5, Mockito, Testcontainers, ArchUnit).
