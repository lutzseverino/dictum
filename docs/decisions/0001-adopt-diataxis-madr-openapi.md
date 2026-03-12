---
title: Adopt Diataxis, MADR, and OpenAPI for project documentation
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-12
owner: Engineering
doc-type: decision
summary: Record the decision to standardize Dictum documentation around Diataxis, MADR, and OpenAPI.
---

# Adopt Diataxis, MADR, and OpenAPI for project documentation

## Status

Accepted

## Context

Dictum needs a documentation system that is durable, understandable to other contributors, and grounded in widely recognized open standards instead of repo-specific conventions.

The project also has distinct documentation needs:

- human-readable explanation and contributor guidance
- durable decision history
- machine-readable API contracts

## Decision

Dictum documentation will use:

- Diataxis as the overall documentation taxonomy
- MADR for architecture and engineering decision records
- OpenAPI for HTTP contract definition

The repository will organize docs under:

- `tutorials/`
- `how-to/`
- `reference/`
- `explanation/`
- `decisions/`
- `openapi/`

## Consequences

- Documentation structure aligns with established standards instead of a custom repo-only scheme.
- Decision records gain a stable, recognizable format.
- HTTP contracts have a clear home separate from prose documentation.
- Contributors must learn and respect the Diataxis distinction between tutorial, how-to, reference, and explanation writing.

## Alternatives Considered

- Reuse the existing custom docs structure as-is
  Rejected because it is clean but repo-specific rather than a broadly recognized standard.
- Delay docs structure decisions until more documentation exists
  Rejected because the repository already has contracts and architecture choices that benefit from an explicit home.

