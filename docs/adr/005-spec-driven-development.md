# ADR-005: Spec-Driven Development (SDD)

## Status
Accepted

## Context
We need a development methodology that ensures features are well-defined before implementation, tests are traceable to requirements, and documentation stays in sync with the code.

## Decision
Spec-Driven Development with lightweight markdown specs.

Workflow:
1. Write a spec in `docs/specs/<context>/<feature>.md` using the standardized format (defined in CLAUDE.md).
2. Review the spec — all edge cases, invariants, and acceptance criteria must be defined. Open questions section must be empty.
3. Derive test cases from the spec's acceptance criteria.
4. TDD within the implementation: write failing tests → implement → green → refactor.
5. Verify implementation matches spec.

Spec format is structured markdown with mandatory sections: Context, Description, Domain model, Rules and invariants, Commands and queries, Behavior (happy path + edge cases), Acceptance criteria (GIVEN/WHEN/THEN), API contract, Dependencies, Open questions.

We chose lightweight markdown over formal specs (OpenAPI, AsyncAPI) because:
- More readable for both humans and AI agents
- No code generation tooling to configure and maintain
- Tests are written manually from specs, ensuring the developer understands every test case
- Lower maintenance burden for a single-developer project

## Consequences
- **Every feature has a spec before code.** No implementation without a written, reviewed spec.
- **Tests are traceable.** Each acceptance criterion maps to one or more tests.
- **Specs require maintenance.** If the implementation diverges, the spec must be updated.
- **No auto-generation.** Stubs, mocks, and contract tests are written manually. This is more work but ensures deeper understanding.
