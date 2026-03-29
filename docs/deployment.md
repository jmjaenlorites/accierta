# Deployment Guide

## Context

This document explains how Accierta is deployed to staging and production after ADR-008 and ADR-009.

It is an **operational guide**, not a feature spec. It defines environments, domains, CI/CD expectations, and the responsibilities of Cloudflare Pages, Coolify, and GitHub.

## Deployment topology

Accierta is a monorepo with two deployable applications:
- Backend: Spring Boot modular monolith at repository root
- Frontend: React PWA in `frontend/`

Public domains:

| Environment | Web | API | Hosted on |
|-------------|-----|-----|-----------|
| Production | `https://accierta.com` | `https://api.accierta.com` | Pages + Coolify |
| Staging | `https://staging.accierta.com` | `https://api-staging.accierta.com` | Pages + Coolify |

Infrastructure split:
- **Cloudflare Pages** serves the frontend build artifacts
- **Coolify** runs the backend container and PostgreSQL
- **Cloudflare DNS / proxy** exposes public domains and TLS

## Environments

### Production

- Branch: `main`
- Web domain: `accierta.com`
- API domain: `api.accierta.com`
- Database: dedicated production PostgreSQL instance
- Secrets: production-only

### Staging

- Branch: `staging`
- Web domain: `staging.accierta.com`
- API domain: `api-staging.accierta.com`
- Database: dedicated staging PostgreSQL instance
- Secrets: staging-only

### Environment separation rules

These rules always apply:
- Production and staging never share a database
- Production and staging never share JWT secrets
- Production and staging never share email credentials or sender configuration unless that is an intentional temporary choice
- Production and staging never share domain names
- Staging must be safe to reset without affecting production

## DNS and Cloudflare

Required DNS records:

| Name | Type | Target | Proxy |
|------|------|--------|-------|
| `@` | CNAME / A | Cloudflare Pages project or server IP, depending on final web setup | Proxied |
| `staging` | CNAME | Cloudflare Pages staging project | Proxied |
| `api` | A / CNAME | Coolify server | Proxied |
| `api-staging` | A / CNAME | Coolify server | Proxied |

Cloudflare configuration:
- SSL mode: `Full (strict)`
- WebSockets: enabled
- Always use HTTPS: enabled
- Optional later: caching rules for static assets, WAF rules, bot protection, rate limiting for auth endpoints

## Cloudflare Pages

Create one frontend deployment per environment.

### Production Pages project

- Repository: this monorepo
- Root directory: `frontend`
- Production branch: `main`
- Build command: `npm ci && npm run build`
- Output directory: `dist`

Frontend production variables:
- `VITE_API_BASE_URL=https://api.accierta.com`
- `VITE_WS_BASE_URL=wss://api.accierta.com/ws`

### Staging Pages project

- Repository: this monorepo
- Root directory: `frontend`
- Production branch: `staging`
- Build command: `npm ci && npm run build`
- Output directory: `dist`

Frontend staging variables:
- `VITE_API_BASE_URL=https://api-staging.accierta.com`
- `VITE_WS_BASE_URL=wss://api-staging.accierta.com/ws`

### Preview deployments

Cloudflare Pages can create preview deployments for pull requests. These are useful for UI validation, but they do not replace the staging environment.

Use previews for:
- visual checks
- basic navigation checks
- sharing in-progress frontend changes

Do not use previews as the source of truth for backend-integrated validation. That remains the role of `staging`.

## Coolify

Create separate backend applications for production and staging.

### Production backend

- Repository: this monorepo
- Branch: `main`
- Build context: repository root
- Dockerfile: `Dockerfile`
- Domain: `api.accierta.com`

### Staging backend

- Repository: this monorepo
- Branch: `staging`
- Build context: repository root
- Dockerfile: `Dockerfile`
- Domain: `api-staging.accierta.com`

### PostgreSQL

Provision one PostgreSQL service per environment:
- `postgres-prod`
- `postgres-staging`

Do not point both backend apps at the same database.

### Backend environment variables

At minimum, each Coolify backend app needs:

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `ACCIERTA_SECURITY_JWT_SECRET`
- `ACCIERTA_GROUPS_INVITATION_BASE_URL`
- Resend-related secrets

Recommended values:

| Variable | Production | Staging |
|----------|------------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | `staging` |
| `ACCIERTA_GROUPS_INVITATION_BASE_URL` | `https://accierta.com/invite` | `https://staging.accierta.com/invite` |

Backend runtime requirements:
- trust forwarded headers from the reverse proxy
- configure CORS for the approved frontend origins only
- configure WebSocket allowed origins for the approved frontend origins only
- expose a health endpoint suitable for platform health checks

## CI/CD

### Branch strategy

- `main` deploys production
- `staging` deploys staging
- feature branches are short-lived and merged through pull requests

This keeps the environments easy to understand:
- branch -> environment
- domain -> environment
- deploy target -> environment

### Protected branches

Protect at least:
- `main`
- `staging`

Recommended branch protection rules:
- require pull requests before merging
- disable direct pushes
- require passing status checks
- require branch to be up to date before merge
- require conversation resolution before merge
- allow only administrators to bypass in emergencies

Required status check to enable first:
- `CI Status`

For private repositories on GitHub Free, use repository rules and required checks instead of GitHub environments. Environment-scoped secrets and approval gates for private repositories require a paid plan.

Optional but recommended:
- prefer squash merge or rebase merge for a clean history
- restrict who can push tags used for releases

### CI jobs for this monorepo

Recommended GitHub Actions structure:

1. `ci-backend`
2. `ci-frontend`
3. `ci-docs` (optional)

`ci-backend` should run when:
- files under `src/` change
- `build.gradle.kts` changes
- `settings.gradle.kts` changes
- `Dockerfile` changes
- shared files affecting backend behavior change

Suggested backend checks:
- `docker build .`

In this repository, backend CI intentionally validates through the Docker build, because the backend is designed to run inside Docker and the current `Dockerfile` already runs `gradle test bootJar` during image creation. This keeps runtime and CI aligned.

`ci-frontend` should run when:
- files under `frontend/` change
- frontend deployment config changes
- shared files affecting frontend behavior change

Suggested frontend checks:
- `npm ci`
- `npm run build`

At the moment, the repository has existing frontend lint violations. Because of that, the initial CI workflow uses the production build as the required frontend check and leaves lint as a manual or follow-up hardening step. Once the frontend baseline is lint-clean, `npm run lint` should be promoted to a required CI check.

`ci-docs` can run when:
- only markdown/docs files change

Suggested docs checks:
- markdown lint if you want it
- link validation if it stays low-friction

### How CI/CD works in a monorepo

A monorepo means one Git repository contains multiple applications. CI/CD usually handles this with **path-aware workflows**.

That means:
- frontend jobs only run when frontend-related files changed
- backend jobs only run when backend-related files changed
- both jobs run when shared files changed

This matters because otherwise every pull request would run every job, which gets slower and more expensive over time.

For this repository, you do **not** need heavy monorepo tooling yet. GitHub Actions path filters are enough.

### Deployment flow

Recommended flow:

1. Open PR into `staging`
2. CI validates changed parts of the monorepo
3. Merge into `staging`
4. Cloudflare Pages deploys `staging.accierta.com` from Git integration
5. Coolify deploys `api-staging.accierta.com` from Git integration
6. Manual verification happens on staging
7. Open PR from `staging` to `main`
8. CI runs again
9. Merge into `main`
10. Cloudflare Pages deploys production frontend from Git integration
11. Coolify deploys production backend from Git integration

This is intentionally explicit. It creates a real pre-production step instead of deploying straight from feature branches to production.

## Manual verification before production

Before merging `staging` into `main`, verify at least:
- web app loads on the staging domain
- login and registration flows work against the staging API
- invitation links point to the staging web domain
- WebSocket updates work through the staging API domain
- backend health checks are green
- database writes land in the staging database, not production

## Operational notes for a monorepo

Important monorepo considerations:
- Frontend and backend version together at the repository level, even if they deploy independently
- A backend API change may require a frontend deploy from the same commit series
- Shared documentation and ADR changes can affect both deployable apps indirectly
- Deployment coordination matters more than package versioning

What to watch for:
- avoid assuming that frontend and backend always deploy at the exact same moment
- prefer backward-compatible API changes when possible
- if a change requires coordinated rollout, document it in the PR and deploy plan

For this project, independent deployment with coordinated branch promotion is the right balance.
