---
title: Keep entities inside service boundaries
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-12
owner: Engineering
doc-type: decision
summary: Record the decision to keep entities internal to persistence and service implementation details while exposing only DTOs at controller and service boundaries.
---

# Keep entities inside service boundaries

## Status

Accepted

## Context

Dictum is building a backend control plane that will eventually expose multiple resources, commands, and provider-backed workflows. The project wants to preserve clean endpoint navigation, stable API contracts, and room to evolve persistence models independently.

There was also alignment on keeping controllers minimal and using consistent service naming for read endpoints.

## Decision

Dictum will enforce these backend boundaries:

- Controllers make one service call per endpoint method.
- Controllers accept and return DTOs only.
- Public service methods return DTOs, not entities.
- Entities remain internal to persistence and service implementation details.
- Services are grouped by resource first, then split into query and command responsibilities as needed.
- Read flows prefer projections or read models mapped to DTOs.
- Query services use `getResponse(...)` for singular reads and `listResponses(...)` for collection reads.
- Command services use explicit verbs for mutations.
- MapStruct is the preferred mapping approach between projections/entities and DTOs.

## Consequences

- Backend contracts remain decoupled from persistence structure.
- Controller classes stay easy to scan and document.
- Service APIs become predictable across resources.
- Query and command paths are clearer than a generic CRUD abstraction.
- The service layer avoids both a giant catch-all resource service and a one-class-per-action explosion.
- The project accepts extra mapper and DTO boilerplate in exchange for stronger boundaries.

## Alternatives Considered

- Allow entities to move through services and occasionally through controllers
  Rejected because it weakens the API boundary and couples external behavior to persistence structure.
- Use a generic CRUD service abstraction with uniform endpoint methods
  Rejected because it hides intent and makes non-trivial endpoint behavior harder to express cleanly.
- Create one top-level service class per resource action
  Rejected as the default because it fragments the service layer too early and makes endpoint flows harder to scan at the current scale.
