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

Additional production browser origin:
- `https://www.accierta.com`

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
- Staging emails include a `[STAGING]` prefix in the subject line to distinguish them from production emails. This is configured via `accierta.email.subject-prefix` in `application-staging.yml`. Production explicitly sets this to empty.
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

Recommended web-domain policy:
- keep `accierta.com` as the canonical production web domain
- optionally redirect `www.accierta.com` to `accierta.com`
- still allow `www.accierta.com` as a valid backend origin to avoid browser failures during rollout or redirect transitions

### Email Routing

Inbound email for legal and privacy contacts is handled by **Cloudflare Email Routing** (free tier). No mail server or additional provider is needed — Cloudflare forwards incoming messages to a personal email address.

Configured routes:

| Address | Forwards to | Purpose |
|---------|-------------|---------|
| `privacy@accierta.com` | personal email | Privacy policy contact (GDPR rights, data requests) |
| `legal@accierta.com` | personal email | Terms of Service contact (legal inquiries) |
| `contact@accierta.com` | personal email | General contact |

These addresses are referenced in the landing page footer links (`/privacy`, `/terms`).

Setup: Cloudflare Dashboard → Email → Email Routing → Create rule. Cloudflare automatically provisions the required MX and TXT DNS records.

Outbound transactional email (registration, password reset, invitations) continues to use **Resend**. Cloudflare Email Routing and Resend do not conflict — they handle different directions (inbound vs outbound).

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

### First-time setup order

Recommended setup order:
1. Create the production Pages project
2. Create the staging Pages project
3. Connect custom domains for web
4. Create the production PostgreSQL database in Coolify
5. Create the staging PostgreSQL database in Coolify
6. Create the production backend application in Coolify
7. Create the staging backend application in Coolify
8. Configure backend environment variables
9. Configure API DNS records in Cloudflare
10. Deploy and validate staging before production

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

### Runtime configuration

Frontend configuration lives in Cloudflare Pages environment variables:

| Variable | Production | Staging |
|----------|------------|---------|
| `VITE_API_BASE_URL` | `https://api.accierta.com` | `https://api-staging.accierta.com` |
| `VITE_WS_BASE_URL` | `wss://api.accierta.com/ws` | `wss://api-staging.accierta.com/ws` |

### Backend environment variables

At minimum, each Coolify backend app needs:

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `ACCIERTA_SECURITY_JWT_SECRET`
- `ACCIERTA_GROUPS_INVITATION_BASE_URL`
- `ACCIERTA_EMAIL_API_KEY`

Recommended values:

| Variable | Production | Staging |
|----------|------------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | `staging` |
| `ACCIERTA_GROUPS_INVITATION_BASE_URL` | `https://accierta.com/invite` | `https://staging.accierta.com/invite` |
| `ACCIERTA_EMAIL_API_KEY` | Resend API key | Resend API key |

Backend runtime requirements:
- trust forwarded headers from the reverse proxy
- configure CORS for the approved frontend origins only
- configure WebSocket allowed origins for the approved frontend origins only
- expose a health endpoint suitable for platform health checks

Approved browser origins by environment:
- Production:
  - `https://accierta.com`
  - `https://www.accierta.com`
- Staging:
  - `https://staging.accierta.com`

This is enforced through Spring profile configuration:
- [application-prod.yml](/Users/jmjaenlorites/MyProjects/accierta/src/main/resources/application-prod.yml)
- [application-staging.yml](/Users/jmjaenlorites/MyProjects/accierta/src/main/resources/application-staging.yml)

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

### Monorepo deployment tradeoff

Cloudflare Pages and Coolify both deploy from Git integration at the repository + branch level.

That means:
- a commit that changes only `frontend/` still creates a new commit on the tracked branch
- Cloudflare Pages will rebuild the frontend, which is expected
- Coolify will also see a new commit on the same branch and may rebuild the backend, even if no backend files changed

This is an intentional tradeoff in the current setup:
- **Pros:** simpler operational model, fewer moving parts, no webhook/token deployment pipeline to maintain
- **Cons:** backend rebuilds can happen for frontend-only commits, and vice versa if another provider watches the same branch

We accept this inefficiency for now because the project is still small and deployment simplicity is more valuable than perfect path-aware CD.

If this becomes too costly later, the next step is:
- keep GitHub Actions for CI
- move backend deployment triggers back to GitHub Actions with path filtering
- let Coolify deploy only when backend-related paths changed

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

GitHub Actions is used for CI only:
- validate backend through `docker build`
- validate frontend through production build
- provide the `CI Status` check used by branch protection

## Manual verification before production

Before merging `staging` into `main`, verify at least:
- web app loads on the staging domain
- login and registration flows work against the staging API
- invitation links point to the staging web domain
- WebSocket updates work through the staging API domain
- backend health checks are green
- database writes land in the staging database, not production

Minimum post-deploy checks:
- `https://staging.accierta.com` loads the frontend successfully
- browser requests go to `https://api-staging.accierta.com`
- CORS succeeds from the staging web domain
- WebSocket handshake succeeds from the staging web domain
- invitation links generated by the backend point to `https://staging.accierta.com/invite`
- production keeps accepting both `accierta.com` and `www.accierta.com` as browser origins

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
