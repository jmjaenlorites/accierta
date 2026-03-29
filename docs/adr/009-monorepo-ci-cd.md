# ADR-009: Monorepo CI/CD with protected branches and environment-specific deployments

## Status
Accepted

## Context
Accierta is a monorepo with two deployable applications:
- the backend Spring Boot application in the repository root
- the frontend React PWA in `frontend/`

The deployment topology is already defined in ADR-008:
- frontend on Cloudflare Pages
- backend and PostgreSQL on Coolify
- separate production and staging environments

We need a CI/CD strategy that fits a monorepo, keeps production safe, supports staging, and stays understandable for a developer who is new to deployment automation.

The main questions are:
- which branches represent production and staging
- what checks must run on pull requests
- which branches must be protected
- how frontend and backend deploy independently from the same repository
- what a reasonable monorepo pipeline looks like without introducing unnecessary tooling

## Decision
Use a GitHub-based CI/CD model with two long-lived deployment branches and path-aware workflows.

Branch model:
- `main` is the production branch
- `staging` is the pre-production branch
- feature work happens in short-lived branches created from `main` or `staging`, depending on the target

Branch protection:
- `main` and `staging` are protected branches
- direct pushes are disabled on both branches
- all changes arrive through pull requests
- at least one successful CI run is required before merge
- linear history is preferred to keep deployment history easy to follow

CI model:
- Pull requests to `main` and `staging` run validation workflows
- The backend and frontend are validated independently, based on path filters
- Shared repository files that affect both applications trigger both validation workflows
- Documentation-only changes do not trigger full deployment validation unless the changed docs define behavior that should be verified manually

CD model:
- Merges to `main` deploy production
- Merges to `staging` deploy staging
- Frontend and backend deploy independently
- Cloudflare Pages handles frontend deployments
- Coolify handles backend deployments from the selected branch

Recommended workflow split:
- `ci-backend`: Gradle build, unit tests, integration tests, architecture tests
- `ci-frontend`: npm install, lint, typecheck, production build
- `ci-docs`: optional lightweight markdown/docs validation

Recommended deployment trigger model:
- Frontend production deploy on push to `main`
- Frontend staging deploy on push to `staging`
- Backend production deploy from `main` in Coolify
- Backend staging deploy from `staging` in Coolify

Why this option:
- **Fits the current scale.** Two deployable apps in one repo do not justify a more complex build platform yet.
- **Keeps deployment intent visible.** `main` means production; `staging` means pre-production.
- **Works well with a monorepo.** Each application can validate and deploy only when affected.
- **Matches the chosen hosting split.** Cloudflare Pages and Coolify each do the part they are good at.
- **Reduces accidental production changes.** Protected branches and required checks enforce discipline.

Rejected alternatives:
- **Single branch with ad hoc manual deploys:** simpler at first, but creates ambiguity about what is deployed where and makes rollback reasoning harder.
- **Full trunk-based deployment directly from `main` only:** faster, but removes a realistic pre-production environment that is useful while the product and infrastructure are still changing.
- **Separate repositories for frontend and backend:** unnecessary split at this stage. The codebase is still one product with coordinated domain and API evolution.
- **Heavy monorepo tooling (Nx, Turborepo, Bazel):** too much operational complexity for two applications and one developer.

## Consequences
- **A `staging` branch must exist and be maintained.** This adds a small amount of branch management overhead.
- **CI must be path-aware.** Monorepo workflows need to know whether frontend, backend, or both changed.
- **Deployments are easier to reason about.** Every environment maps to a branch and a domain.
- **Rollbacks are simpler.** Reverting a merge on `main` or `staging` maps directly to the affected environment.
- **The pipeline stays incremental.** If the monorepo grows, more path-aware jobs can be added without changing the branch model.
