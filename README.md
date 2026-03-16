<div align="center">
    <h1 align="center">Dictum</h1>
    <p>An open-source control plane for developer-owned personal blogs.</p>
    <p>
        <img alt="status" src="https://img.shields.io/badge/status-foundation-0f172a">
        <img alt="frontend" src="https://img.shields.io/badge/frontend-next.js-111827">
        <img alt="backend" src="https://img.shields.io/badge/backend-spring_boot-1f2937">
    </p>
</div>

## Overview

Dictum is an open-source control plane for developer-owned personal blogs. Its product surface is the publishing API, the admin experience, and the content contracts needed to manage a blog cleanly without owning the public presentation layer.

Your public blog frontend is expected to live in a separate repository and consume Dictum through its HTTP API and shared contracts. Dictum may later offer managed hosting for the control plane and connected blog frontends, but the core open-source product remains the control plane itself.

The repository currently establishes the product architecture and development foundations rather than a full end-user product.

Project documentation follows Diataxis for structure, MADR for decision records, and OpenAPI for HTTP contracts. Start in [docs/README.md](./docs/README.md).

## Architecture

- `services/api` owns the control-plane API, orchestration boundaries, and future auth/provider integrations.
- `apps/admin` hosts the mobile-first admin shell that belongs to Dictum's core product surface.
- `packages/api-client` exposes the generated TypeScript client for the control plane.
- `packages/rendering` defines the markdown/content contracts shared by Dictum and external blog consumers.

## Repository Layout

```text
dictum/
  apps/
    admin/
  packages/
    api-client/
    rendering/
  services/
    api/
  docs/
    README.md
    tutorials/
    how-to/
    reference/
    explanation/
    decisions/
    openapi/
```

## Development

1. Install frontend dependencies from the repo root with `pnpm install`.
2. Run the admin app with `pnpm dev:admin`.
3. Run the API with `pnpm dev:api`.
4. Check the frontend workspace with `pnpm lint:web` and `pnpm typecheck:web`.
5. Check Java formatting and baseline requirements with `pnpm lint:api`.
6. Apply Google Java Format to the API with `pnpm format:api`.
7. Run API tests with `pnpm test:api`.

## Content Model

- The future content source of truth lives in a separate `dictum-content` repository.
- Posts remain markdown-first.
- Per-post styling is represented as optional plain CSS sidecars, not embedded Tailwind utilities in markdown.

See [docs/reference/content-repository-contract.md](./docs/reference/content-repository-contract.md) for the expected content layout.

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Current Scope

- Establish the control-plane module boundaries and contracts.
- Expose post and site-settings control-plane endpoints plus OpenAPI docs.
- Keep the admin app in the workspace as a core product surface for future UI work.
- Preserve a markdown-first content contract for externally owned blog frontends.
- Keep managed hosting, auth hardening, and deployment integrations as explicit future boundaries.

## Out of Scope

- Treating a public blog frontend as part of Dictum's required product surface
- Shipping a canonical blog theme or design system as a product commitment
- Expanding into newsletters, memberships, or broader publication-platform concerns
- Becoming a generic CMS for arbitrary content domains
