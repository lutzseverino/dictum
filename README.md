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

Dictum is an open-source control plane for developer-owned personal blogs.

It provides:

- a control-plane API for posts and site settings
- a browser-based admin app for managing a blog
- shared contracts for markdown-first content workflows

The public blog frontend lives outside this repository and consumes Dictum through its API and content contracts.

## Architecture

- `services/api` owns the control-plane API and orchestration boundaries.
- `apps/admin` hosts the mobile-first admin app.
- `packages/api-client` exposes the generated TypeScript client for the control plane.
- `packages/content-contract` defines the content types and repository contracts shared by Dictum and external blog consumers.

## Repository Layout

```text
dictum/
  apps/
    admin/
  packages/
    api-client/
    content-contract/
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
2. Set `DICTUM_CONTENT_ROOT` to the root of a content repository that matches the Dictum content contract.
3. Run the admin app with `pnpm dev:admin`.
4. Run the API with `pnpm dev:api`.
5. Check the frontend workspace with `pnpm lint:web` and `pnpm typecheck:web`.
6. Check Java formatting and baseline requirements with `pnpm lint:api`.
7. Apply Google Java Format to the API with `pnpm format:api`.
8. Run API tests with `pnpm test:api`.
9. Start a local SonarQube Community Build stack with `pnpm sonar:start`.
10. Run the full validation flow with `pnpm sonar:validate`; if SonarQube is not configured locally, the script can offer Docker-based local setup in an interactive terminal and otherwise warns and skips only the Sonar scan.

## Content Model

- The content source of truth lives in an external content repository.
- Posts remain markdown-first.
- Per-post styling is represented as optional plain CSS sidecars, not embedded Tailwind utilities in markdown.
- The API reads and writes that repository through `dictum.content.root` / `DICTUM_CONTENT_ROOT`.

See [docs/reference/content-repository-contract.md](./docs/reference/content-repository-contract.md) for the expected content layout.

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Product

- Control-plane API for publishing and blog management
- Admin web app for managing posts and site settings
- Markdown-first content contract for externally owned blog frontends
- Typed API client generated from the OpenAPI contract

## Quality Tooling

- SonarQube Community Build: `http://localhost:9000`
- SonarQube scan configuration: `sonar-project.properties`
- Local SonarQube workflow: [docs/how-to/run-sonarqube-analysis.md](./docs/how-to/run-sonarqube-analysis.md)

## Non-Goals

- Owning the public blog frontend
- Shipping a canonical blog theme or design system
- Expanding into newsletters, memberships, or broader publication-platform concerns
- Becoming a generic CMS for arbitrary content domains
