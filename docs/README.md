---
title: Documentation
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-16
owner: Engineering
doc-type: index
summary: Define the documentation structure for Dictum using Diataxis, MADR, and OpenAPI, including the product-boundary documents that describe Dictum as a blog control plane.
---

# Documentation

## Purpose

Organize Dictum documentation around established open standards so architecture, contributor guidance, API contracts, and decision history stay durable and easy to navigate.

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

## Current Documents

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

## Authoring Rules

- Use YAML frontmatter in every tracked document.
- Keep metadata explicit: title, status, author, created, updated, owner, doc-type, and summary.
- Put content in the Diataxis area that matches the reader's need instead of the author's preference.
- Use `docs/decisions/` only for durable decisions with meaningful tradeoffs.
- Keep machine-readable HTTP contracts under `docs/openapi/`, even when they are also exposed by the running service.
