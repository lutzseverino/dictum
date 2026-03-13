---
title: Adopt spec-first code generation
status: accepted
author: Codex
created: 2026-03-13
updated: 2026-03-13
owner: Engineering
doc-type: decision
summary: Use the checked-in OpenAPI contract as the source of truth for generated frontend and backend contract artifacts.
---

# Adopt spec-first code generation

## Status

Accepted

## Context

Dictum has chosen an OpenAPI-first workflow for its first real API slice. The project needs deterministic contract sharing between the Spring Boot backend and the TypeScript frontend workspace without letting generated code take over handwritten controller and service architecture.

## Decision

Dictum treats `docs/openapi/dictum.yaml` as the canonical HTTP contract and generates:

- frontend TypeScript client and models from the YAML
- backend Spring interfaces and API models from the YAML

Generated backend code is limited to interfaces and models. Handwritten controllers, services, and internal state remain the implementation ownership model.

## Consequences

- Frontend and backend contract artifacts stay deterministic and reviewable.
- The backend keeps its documented controller and service conventions.
- Contract changes now require regeneration as part of the normal development workflow.
- Generated code becomes derived output that must not be hand-edited.

## Alternatives Considered

- Handwrite all DTOs and clients and treat the YAML as documentation only.
- Generate full backend controller stubs and let generation drive the Spring architecture.
