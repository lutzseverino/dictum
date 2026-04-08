---
title: HTTP API Conventions
status: accepted
author: Codex
created: 2026-03-13
updated: 2026-04-08
owner: Engineering
doc-type: explanation
summary: Explain the conventional REST and OpenAPI rules used for Dictum control-plane endpoints.
---

# HTTP API Conventions

## Purpose

Dictum prefers conventional REST resource design for its HTTP API.

### Resource Orientation

Endpoints should be modeled as resources first.

- Use plural resource paths such as `/api/v1/posts`.
- Use singular nested resources when the domain is naturally singular, such as `/api/v1/settings/site`.
- Prefer standard HTTP verbs over custom RPC-style endpoints.

### Reads and Writes

Use standard verbs whenever the resource model remains clear.

- `GET` for reads
- `POST` for creates
- `PATCH` for partial updates

When a `PATCH` endpoint follows JSON Merge Patch semantics, document the request body as `application/merge-patch+json`.

When a state transition does not fit cleanly into CRUD, use an action subresource rather than a generic command endpoint.

Example:

- `POST /api/v1/posts/{slug}/publish`

Dictum intentionally avoids a catch-all `/commands` endpoint as its default API shape.

### Response Shapes

Responses should expose the resource directly rather than wrapping everything in a custom envelope.

- Return arrays for collection endpoints.
- Return a single resource document for singular endpoints.
- Avoid custom top-level wrappers such as `{ data: ... }` unless a future requirement makes them necessary.

### Errors

Use Problem Details for HTTP APIs.

- Error responses should use `application/problem+json`.
- Error payloads should follow the standard `type`, `title`, `status`, `detail`, and `instance` structure.
- Dictum extends Problem Details with a stable `code` and structured `params`.
- Backend code owns `type`, `code`, `params`, and fallback problem text.
- Frontend code should render user-facing API errors from `code` and `params`.

#### Problem Field Semantics

- `title` is a coarse generic problem label.
- `detail` is fallback or diagnostic text and not primary UX copy.
- `code` is the stable machine-readable client key.
- `params` contains stable interpolation values only.

#### Problem Code Taxonomy

Problem codes should follow `domain.reason`.

Examples:

- `post.not_found`
- `post.already_exists`
- `auth.unauthenticated`
- `request.invalid`
- `request.method_not_allowed`

The code should stay stable even when fallback text changes.

#### Problem Params

Use `params` only for stable structured values that clients can safely interpolate.

Good examples:

- `slug`
- `field`
- `contentType`
- `method`

Do not put prose, rendered sentences, or localization-oriented copy into `params`.

#### Client Rendering Contract

- Clients should localize API-originated messages from `code`.
- Clients should interpolate those messages with `params`.
- Clients should not rely on `title` or `detail` for localization logic.
- `detail` is intended for fallback and debugging rather than primary user-facing copy.

### Authentication

Dictum protects control-plane endpoints with authenticated sessions.

- The first auth slice uses cookie-backed sessions.
- `POST /api/v1/session` creates a session.
- `GET /api/v1/session` returns the current authenticated session.
- `DELETE /api/v1/session` invalidates the current authenticated session.
- Unsafe authenticated requests must include `X-CSRF-TOKEN`.

### Versioning

Version public HTTP endpoints in the path.

- Dictum uses `/api/v1/...` for the initial contract.
