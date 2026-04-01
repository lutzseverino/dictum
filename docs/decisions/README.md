---
title: Decision Records
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-04-01
owner: Engineering
doc-type: index
summary: Index MADR-style decision records for Dictum.
---

# Decision Records

## Purpose

Preserve durable decisions, tradeoffs, and rejected alternatives so future contributors can understand why Dictum chose a particular path.

## Documents

- [0001: Adopt Diataxis, MADR, and OpenAPI for project documentation](./0001-adopt-diataxis-madr-openapi.md)
- [0002: Keep entities inside service boundaries](./0002-keep-entities-inside-service-boundaries.md)
- [0003: Adopt spec-first code generation](./0003-adopt-spec-first-code-generation.md)
- [0004: Define control-plane product scope](./0004-define-control-plane-product-scope.md)

## Scope Boundaries

- Decision records capture why an option was chosen.
- Active factual material belongs in [Reference](../reference/README.md).
- Broader conceptual background belongs in [Explanation](../explanation/README.md).

## Record Structure

Decision records in this repository use one standard MADR-style shape.

Required sections:

- `Status`
- `Context`
- `Decision`
- `Consequences`
- `Alternatives Considered`

Use the template in [decision.template.md](../_templates/decision.template.md) for new records.

## Writing Rules

- Keep records decision-shaped rather than proposal-shaped.
- State the chosen option directly in `Decision`.
- Keep `Context` limited to the pressure that made the decision necessary.
- Use `Consequences` for actual effects and tradeoffs, not implementation plans.
- Use `Alternatives Considered` for rejected options and why they were not chosen.
- If the repository ever changes decision-record structure, update the template and this index together.
