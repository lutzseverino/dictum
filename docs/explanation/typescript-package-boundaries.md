---
title: TypeScript Package Boundaries
status: accepted
author: Codex
created: 2026-03-31
updated: 2026-03-31
owner: Engineering
doc-type: explanation
summary: Explain how Dictum separates TypeScript contract packages, generated clients, and app-local behavior.
---

# TypeScript Package Boundaries

Dictum keeps TypeScript package responsibilities narrow.

## Package Roles

- `packages/api-client` owns the generated HTTP client for the control-plane API.
- `packages/content-contract` owns shared content types and repository-shape constants.
- `apps/admin` owns admin-app behavior, orchestration, and app-local helpers.

## Dependency Direction

- Apps may depend on packages.
- `packages/content-contract` must not depend on app code.
- `packages/api-client` must not depend on app code.
- `packages/content-contract` must not depend on `packages/api-client`.

## Content Contract Package

`packages/content-contract` is a pure contract package.

It may contain:

- shared content types
- repository-shape constants

It must not contain:

- runtime service abstractions
- in-memory service implementations
- derived consumer helpers
- app-specific behavior

## Helper Behavior

Consumer-side helpers do not belong in a pure contract package.

Examples include:

- reading-time calculation
- summary derivation
- in-memory content services

Until a truly shared runtime package is earned, that behavior should stay in app-local code.

## New Shared Packages

Do not create new TypeScript shared packages speculatively.

Create a new shared package only when:

- more than one consumer needs the same behavior
- the ownership is clearly broader than a single app
- the package responsibility can be named cleanly

## Consequences

- Contract packages stay predictable and easy to trust.
- Generated HTTP concerns and content-contract concerns stay separate.
- App-local convenience code does not harden into accidental shared architecture.
