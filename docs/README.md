---
title: Documentation
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-25
owner: Engineering
doc-type: index
summary: Define the documentation standards for Dictum using Diataxis, MADR, and OpenAPI.
---

# Documentation

## Purpose

Organize Dictum documentation around durable standards so contributor guidance, architecture context, API contracts, and decision history stay easy to navigate as the project evolves.

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
- Place content in the Diataxis area that matches the reader need.
- Tutorials teach a full outcome for readers who are still learning.
- How-to guides assume the reader already knows the goal and needs concise task steps.
- Reference documents should stay authoritative, factual, and easy to scan.
- Explanation documents should read as stable conceptual guidance; move proposals and tradeoff records into decision documents.
- Use `docs/decisions/` only for durable choices with meaningful tradeoffs.
- Product-facing docs and reference docs should be direct and declarative.
- Explanation docs may include rationale, tradeoffs, and conceptual framing when they help the reader understand the current system model.
- Decision docs should use MADR structure.
- How-to guides should stay task-first.
- Keep machine-readable HTTP contracts under `docs/openapi/`.
- Avoid meta-commentary outside explanation, decision, and template documents.

## Documents

- [Documentation Templates](./_templates/README.md)
- [Tutorials](./tutorials/README.md)
- [How-to Guides](./how-to/README.md)
- [Run Java API Quality Checks](./how-to/run-java-api-quality-checks.md)
- [Run SonarQube Analysis](./how-to/run-sonarqube-analysis.md)
- [Reference](./reference/README.md)
- [Explanation](./explanation/README.md)
- [Product Scope and Boundaries](./explanation/product-scope-and-boundaries.md)
- [Decision Records](./decisions/README.md)
- [0004: Define control-plane product scope](./decisions/0004-define-control-plane-product-scope.md)
- [OpenAPI](./openapi/README.md)
