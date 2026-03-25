---
name: docker-env
description: How to manage the Docker development environment for Acerta. Use when starting the project, resetting state, viewing logs, or troubleshooting infrastructure issues. Covers starting/stopping services, database resets, log inspection, and parallel agent setup with worktrees.
user-invocable: false
---

# Docker Development Environment

## Services

The project runs entirely in Docker via `docker compose`:

| Service | Default port | Purpose |
|---------|-------------|---------|
| `app` | 8080 | Spring Boot backend (API + WebSocket) |
| `frontend` | 3000 | React PWA (dev server) |
| `postgres` | 5432 | PostgreSQL database (event store + projections) |

## Common commands

```bash
# Start everything
docker compose up -d

# Start and rebuild (after code changes)
docker compose up -d --build

# Stop everything
docker compose down

# Stop and remove volumes (full reset including database)
docker compose down -v

# View logs
docker compose logs -f app        # Backend logs
docker compose logs -f frontend   # Frontend logs
docker compose logs -f postgres   # Database logs

# Restart a single service
docker compose restart app
```

## Database access

```bash
# Interactive psql session
docker compose exec postgres psql -U acerta -d acerta

# Run a single query
docker compose exec postgres psql -U acerta -d acerta -c "SELECT COUNT(*) FROM events;"
```

## Full reset (clean slate)

```bash
docker compose down -v
docker compose up -d
```

This destroys and recreates the PostgreSQL volume. All events, projections, and user data are wiped. Schema migrations run automatically on startup.

---

## Parallel agents with worktrees

When multiple agents work in parallel (git worktrees), each needs its own isolated Docker environment to avoid port conflicts and shared state.

### Setup for a worktree

Create a `docker-compose.override.yml` in the worktree root with unique ports:

```yaml
# docker-compose.override.yml (worktree-specific, gitignored)
services:
  app:
    ports:
      - "${APP_PORT:-8080}:8080"
  frontend:
    ports:
      - "${FRONTEND_PORT:-3000}:3000"
  postgres:
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    # Unique volume name per worktree to isolate data
    volumes:
      - pgdata_${WORKTREE_ID:-main}:/var/lib/postgresql/data

volumes:
  pgdata_${WORKTREE_ID:-main}:
```

### Port convention

| Instance | APP_PORT | FRONTEND_PORT | POSTGRES_PORT | WORKTREE_ID |
|----------|----------|---------------|---------------|-------------|
| Main | 8080 | 3000 | 5432 | main |
| Worktree 1 | 8081 | 3001 | 5433 | wt1 |
| Worktree 2 | 8082 | 3002 | 5434 | wt2 |

### Starting a worktree environment

```bash
# From the worktree directory
APP_PORT=8081 FRONTEND_PORT=3001 POSTGRES_PORT=5433 WORKTREE_ID=wt1 docker compose up -d
```

### Important rules for agents in worktrees

1. **Always check your ports first** — read `docker-compose.override.yml` or env vars before making curl requests or DB connections.
2. **Never use hardcoded ports** in test commands. Use the configured port.
3. **Each worktree has its own database** — data is not shared between agents.
4. **Clean up when done** — `docker compose down -v` in your worktree before it's removed.

---

## Troubleshooting

### App won't start
```bash
# Check logs for errors
docker compose logs app --tail 50

# Verify PostgreSQL is healthy
docker compose exec postgres pg_isready -U acerta
```

### Port already in use
```bash
# Find what's using the port
lsof -i :8080
lsof -i :3000

# If another worktree agent, use different ports
# If a stale container, stop it: docker compose down
```

### Database migration failed
```bash
# Check migration logs
docker compose logs app | grep -i migration

# Full reset if migration is broken
docker compose down -v
docker compose up -d
```
