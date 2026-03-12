---
title: HTTP API Conventions
status: accepted
author: Codex
created: 2026-03-13
updated: 2026-03-13
owner: Engineering
doc-type: explanation
summary: Explain the conventional REST and OpenAPI rules used for Dictum control-plane endpoints.
---

# HTTP API Conventions

## Problem

Dictum needs an API shape that feels unsurprising to contributors and easy to review in OpenAPI, GitHub, and backend code. Because the project is still in its foundational phase, this is the right time to choose a clear default and avoid accidental drift into custom control-plane patterns.

## Explanation

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

### Versioning

Version public HTTP endpoints in the path.

- Dictum uses `/api/v1/...` for the initial contract.

### Auth in the Contract

The OpenAPI contract may declare bearer authentication before runtime auth is implemented.

This allows the contract to express the intended boundary without forcing the implementation slice to solve auth immediately.

## Consequences

- The API is easier for contributors to understand without project-specific onboarding.
- OpenAPI documents remain close to what readers expect from a conventional REST service.
- Backend code can stay disciplined internally without forcing unconventional HTTP shapes externally.
- Some operations may eventually need action subresources, but that is still more conventional than a generic command bus endpoint.

