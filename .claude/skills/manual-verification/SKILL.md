---
name: manual-verification
description: Mandatory manual verification process before closing a feature or spec. Use this skill after completing implementation and automated tests pass, before marking any feature as done. Covers API testing, database inspection, PWA validation, and regression checks.
user-invocable: false
---

# Manual Verification Process

Before marking any feature or spec as complete, you MUST perform manual verification. Automated tests are necessary but not sufficient — you must validate the feature works as a real user would experience it.

## When to run this process

- After all automated tests pass for a feature/spec
- Before declaring a spec implementation as "done"
- After any change that could affect existing functionality

## Verification checklist

Copy and work through this checklist for every feature:

```
Manual Verification:
- [ ] All automated tests pass (unit, integration, architecture, contract)
- [ ] API endpoints tested with real HTTP requests (see test-api skill)
- [ ] Database state inspected after operations (see inspect-db skill)
- [ ] Event store contains expected events with correct payloads
- [ ] Projections reflect the correct state after events
- [ ] WebSocket messages verified for real-time features
- [ ] PWA UI validated in browser for user-facing changes (see test-pwa skill)
- [ ] Regression: existing features still work after changes
- [ ] Edge cases from spec manually tested (not just in unit tests)
- [ ] Error responses return correct HTTP status codes and messages
```

## Regression testing

When your changes touch shared code (event store, shared kernel, infrastructure), manually verify:

1. **Cross-context events**: if you changed an event, verify all consumers still process it correctly
2. **API contracts**: if you changed a DTO or response shape, verify all endpoints still return the expected format
3. **Database schema**: if you changed a migration, verify existing data is not corrupted

## How to test each layer

- **API endpoints**: read the `test-api` skill
- **Database and event store**: read the `inspect-db` skill
- **PWA / browser UI**: read the `test-pwa` skill
- **Docker environment**: read the `docker-env` skill if you need to start/restart services
