# Dictum

Dictum is a hybrid blog platform skeleton: a Spring Boot control plane, a Next.js public site, and a separate Next.js admin app designed for phone-first management.

## Workspace

- `apps/site` hosts the public blog shell.
- `apps/admin` hosts the mobile-first admin shell.
- `services/api` owns API contracts, orchestration boundaries, and future auth/provider integrations.
- `packages/rendering` defines the markdown/content contracts.
- `packages/site-kit` holds reusable React primitives for the public site.

## Getting Started

1. Install frontend dependencies from the repo root with `pnpm install`.
2. Run the public site with `pnpm dev:site`.
3. Run the admin app with `pnpm dev:admin`.
4. Run the API with `pnpm dev:api`.
5. Check the frontend workspace with `pnpm lint:web` and `pnpm typecheck:web`.

## Contracts

- The future content source of truth lives in a separate `dictum-content` repository.
- Posts remain markdown-first.
- Per-post styling is represented as optional plain CSS sidecars, not embedded Tailwind utilities in markdown.

See `docs/content-repository-contract.md` for the expected content layout.

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
