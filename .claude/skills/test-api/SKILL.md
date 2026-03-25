---
name: test-api
description: How to manually test REST API endpoints using curl. Use when implementing or modifying API endpoints, controllers, or DTOs. Covers authentication, request construction, response validation, and WebSocket testing.
user-invocable: false
---

# Testing API Endpoints

## Base URL

The application runs in Docker. **Check your port first** — it may differ in worktrees:

```bash
# Check configured port (default 8080)
grep -A2 'app' docker-compose.override.yml 2>/dev/null || echo "Using default: 8080"
```

Default: `http://localhost:8080/api`

## Authentication

Most endpoints require a JWT token. To get one:

```bash
# 1. Register a test user (if needed)
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "testpassword123"}' | jq

# 2. Verify email (in dev, check the logs or database for the verification token)
# See inspect-db skill for how to find the token

# 3. Login to get tokens
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "testpassword123"}' | jq

# Save the access token
TOKEN="<paste-access-token-here>"
```

## Making authenticated requests

```bash
curl -s -X GET http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

## What to verify on every endpoint

1. **Happy path**: correct status code (200, 201, 204) and response body
2. **Validation errors**: send invalid input → expect 422 with clear error message
3. **Auth errors**: omit token → expect 401. Use wrong user → expect 403.
4. **Not found**: use non-existent ID → expect 404
5. **Business rule violations**: trigger each rule from the spec → expect correct error

## Testing pattern for a new endpoint

```bash
# 1. Happy path
curl -s -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Group", "initialBalance": 1000}' | jq

# 2. Validation error (empty name)
curl -s -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "", "initialBalance": 1000}' | jq

# 3. Validation error (negative balance)
curl -s -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "initialBalance": -5}' | jq

# 4. No auth
curl -s -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "initialBalance": 1000}' | jq
```

## Testing WebSocket connections

Use `websocat` (install with `brew install websocat`) or curl with upgrade:

```bash
# Connect to a porra channel
websocat "ws://localhost:8080/ws/porra/<porraId>" \
  -H "Authorization: Bearer $TOKEN"

# In another terminal, place a bet to trigger a WebSocket message
curl -s -X POST http://localhost:8080/api/porras/<porraId>/bets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"prediction": "Barcelona"}' | jq

# Verify the WebSocket client receives the expected message
```

## Useful flags

- `| jq` — pretty print JSON responses
- `| jq '.fieldName'` — extract a specific field
- `-v` — verbose mode, shows headers
- `-w "\n%{http_code}\n"` — print status code at the end
- `-s` — silent mode, no progress bar
