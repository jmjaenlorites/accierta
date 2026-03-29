# ADR-008: Decoupled Web and API deployment topology

## Status
Accepted

## Context
We need a production and staging deployment strategy for Accierta. The current local setup uses a frontend container that serves the React PWA and reverse proxies `/api` and `/ws` to the backend container. That setup works well in local Docker Compose, but we now need to decide how the system should be exposed on the public Internet through Coolify and Cloudflare.

This decision also affects future clients. Accierta is starting as a React PWA, but native mobile clients for iOS and Android are likely. If the backend remains reachable only through the web frontend, the architecture becomes web-centric and mobile clients need a second access pattern.

We evaluated three options:
- Keep the current same-origin topology in production: public web server in Coolify, private backend behind frontend reverse proxy.
- Serve the web frontend from Cloudflare Pages and expose the backend as a separate public API.
- Serve the web frontend from Cloudflare Pages and keep same-origin URLs by adding a Cloudflare Worker reverse proxy in front of the backend.

## Decision
Use a decoupled deployment topology:
- The web frontend is deployed separately from the backend.
- The React PWA is served from Cloudflare Pages.
- The backend API and WebSocket endpoints are deployed on Coolify and exposed through dedicated API subdomains.
- Production and staging are fully separated, each with its own frontend deployment, backend deployment, database, secrets, and domain names.

Environment topology:
- Production web: `accierta.com`
- Production API: `api.accierta.com`
- Staging web: `staging.accierta.com`
- Staging API: `api-staging.accierta.com`

Client model:
- The PWA talks directly to the API base URL configured through environment variables.
- Native mobile apps use the same public API and WebSocket endpoints as the PWA.
- The backend is treated as a first-class public application boundary, not as an internal service hidden behind the web frontend.

Why this option:
- **Supports multiple clients cleanly.** Web, iOS, and Android all consume the same API contract.
- **Removes web-hosting coupling.** The backend does not depend on the web frontend being the public entry point.
- **Lets Cloudflare do what it is best at.** Static asset delivery, edge caching, TLS, and preview deployments move off the application server.
- **Keeps Coolify focused on stateful services.** Backend runtime, database, secrets, and application deploys stay on the server.
- **Makes staging realistic.** Staging mirrors production with the same topology and different domains.

Operational requirements implied by this decision:
- The frontend must use explicit environment variables for API and WebSocket base URLs instead of relying on same-origin `/api` and `/ws` paths.
- The backend must allow CORS only for the approved web origins for each environment.
- The backend WebSocket configuration must allow only approved origins for each environment.
- Production and staging must use different databases and secrets, including JWT secrets and email credentials.

Rejected alternatives:
- **Public frontend in Coolify with private backend behind reverse proxy:** valid for a web-only app, but couples the backend to the web entrypoint and becomes awkward once native clients exist.
- **Cloudflare Pages plus Worker reverse proxy for same-origin `/api`:** preserves the current frontend shape, but adds edge proxy complexity without solving a real product problem. It is harder to debug and unnecessary while the API can live on a dedicated subdomain.

## Consequences
- **Frontend configuration changes are required.** The web app must read `VITE_API_BASE_URL` and `VITE_WS_BASE_URL` per environment.
- **Backend hardening becomes mandatory.** CORS, WebSocket allowed origins, forwarded headers, and environment-specific URLs must be configured explicitly.
- **Infrastructure is clearer.** Cloudflare Pages serves stateless assets; Coolify runs the Java app and PostgreSQL.
- **Mobile is unblocked.** Native apps can integrate against the same API the PWA uses.
- **Same-origin simplicity is lost.** The browser client now needs proper CORS handling, which is extra work compared to a reverse-proxy-only setup.
