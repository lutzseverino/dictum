---
title: Documentation
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-16
owner: Engineering
doc-type: index
summary: Define the documentation standards for Dictum using Diataxis, MADR, and OpenAPI.
---

# Documentation

Dictum uses:

- Diataxis for the overall documentation taxonomy
- MADR for decision records
- OpenAPI for HTTP contract definition

## Structure

- `docs/tutorials/`
  Learning-oriented walkthroughs for newcomers
- `docs/how-to/`
  Task-focused guides for contributors and operators
- `docs/reference/`
  Facts, contracts, and lookup material
- `docs/explanation/`
  Architecture concepts, rationale, and deeper system context
- `docs/decisions/`
  MADR-style decision records
- `docs/openapi/`
  Machine-readable API contracts and OpenAPI notes
- `docs/_templates/`
  Starting points for new documentation files

## Writing Rules

- Use YAML frontmatter in every tracked document.
- Keep metadata explicit: title, status, author, created, updated, owner, doc-type, and summary.
- Treat structure and writing style as separate concerns.
- Product-facing docs and reference docs should be direct and declarative.
- Explanation docs may include rationale, tradeoffs, and conceptual framing.
- Decision docs should use MADR structure.
- How-to guides should stay task-first.
- Keep machine-readable HTTP contracts under `docs/openapi/`.
- Avoid meta-commentary outside explanation, decision, and template documents.

## Documents

- [Documentation Templates](./_templates/README.md)
- [Tutorials](./tutorials/README.md)
- [How-to Guides](./how-to/README.md)
- [Run Java API Quality Checks](./how-to/run-java-api-quality-checks.md)
- [Reference](./reference/README.md)
- [Explanation](./explanation/README.md)
- [Product Scope and Boundaries](./explanation/product-scope-and-boundaries.md)
- [Decision Records](./decisions/README.md)
- [0004: Define control-plane product scope](./decisions/0004-define-control-plane-product-scope.md)
- [OpenAPI](./openapi/README.md)
