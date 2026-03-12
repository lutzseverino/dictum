---
title: Backend Layering and Service Boundaries
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-12
owner: Engineering
doc-type: explanation
summary: Explain how Dictum structures controllers, services, entities, projections, and DTOs in the backend.
---

# Backend Layering and Service Boundaries

## Problem

Dictum needs a backend structure that stays easy to navigate as the control plane grows. The project also wants a clear API boundary so controllers, services, persistence, and contract models do not blur together over time.

Without an explicit layering model, entity shapes tend to leak upward into services and controllers, and endpoint behavior becomes harder to document, review, and evolve safely.

## Explanation

Dictum uses a strict outward-facing DTO boundary and keeps controllers deliberately lean.

### Controllers

Controllers are endpoint adapters only.

- Each controller method should make exactly one service call.
- Controllers should not contain orchestration logic, persistence logic, or mapping logic.
- Controllers should accept request DTOs and return response DTOs only.

The goal is for a reader to understand an endpoint quickly by opening the controller and then following a single service method.

### Services

Services act as the gate for each endpoint.

- Services own the endpoint flow, sequencing, and internal structure.
- Services are responsible for validation order, repository interaction, and response assembly.
- Public service methods should stay small and navigable, with private helpers used for internal steps when needed.

Dictum prefers explicit query and command responsibilities over a generic shared CRUD service abstraction.

### Query Services

Query services serve read endpoints.

- Use `getResponse(...)` for singular reads.
- Use `listResponses(...)` for collection reads.
- Query services return response DTOs, not entities.
- Query services should prefer projections or dedicated read models when reading from persistence.

Examples:

- `PostQueryService#getResponse(String slug)`
- `PostQueryService#listResponses()`
- `SiteSettingsQueryService#getResponse()`

### Command Services

Command services serve mutation endpoints.

- Use explicit verbs for mutations such as `publish(...)`, `updateSettings(...)`, or `enqueueProviderJob(...)`.
- Command services may load and mutate entities internally.
- Command services still return response DTOs at their public boundary.

### Entities, Projections, and DTOs

Entities are persistence-internal and must not cross outward into controller contracts.

- Controllers must never expose entities.
- Public service methods must never return entities.
- Read paths should favor `projection -> DTO` or `read model -> DTO`.
- Write paths may use `request DTO -> entity mutation -> response DTO`.

This keeps HTTP contracts stable even when persistence models evolve.

### Mapping

MapStruct is the preferred mapping mechanism for backend DTO assembly.

- Use MapStruct to map projections to response DTOs.
- Use MapStruct to map entities to response DTOs when a write flow needs a response payload.
- Avoid ad hoc controller-level mapping.

The mapping layer is allowed to know about entities and projections, but controllers and external contracts are not.

## Consequences

- Endpoint navigation stays simple because each controller method points to one service method.
- Persistence models can evolve without directly breaking API contracts.
- Read paths gain a natural home for projections and read models.
- Naming stays consistent across services through `getResponse(...)` and `listResponses(...)`.
- Some contributors may find the boundary stricter than a typical Spring CRUD stack, but the tradeoff is deliberate.

