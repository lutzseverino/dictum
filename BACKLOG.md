# Backlog

## Purpose

Track deferred work that matters to the project but is not part of the current implementation focus.

This file is for backlog and sequencing. Durable product, architecture, and reference material belongs in `docs/`.

## Current Focus

- Spring Boot backend cleanup and backend feature work
- API contract clarity
- content-repository runtime behavior

## Deferred

### TypeScript Architecture

Status: deferred until the backend-focused cleanup track is complete

Why it is deferred:

- the TypeScript side is not yet settled enough for a narrow cleanup pass to stay pure
- touching it now would likely produce partial fixes instead of a coherent package model
- the backend now provides a clearer pattern to apply later: contract at the edge, internally owned models, and names that match responsibility

What should be revisited later:

- define standard TypeScript package roles
- clarify what belongs in shared contract packages versus app-local modules
- separate pure contract types from convenience helpers or runtime behavior
- document the TypeScript package model once it is chosen

### `packages/content-contract`

Current question:

- should this package contain only shared contract types and repository-shape constants
- or should it also contain convenience helpers and in-memory service utilities

Types that fit the current package well:

- `PostFrontmatter`
- `BlogPostDocument`
- `BlogPostSummary`
- `SiteSettings`
- `ContentRepositoryContract`
- `dictumContentContract`

Helpers that should be reconsidered later:

- `estimateReadingMinutes`
- `summarizePost`
- `createInMemoryContentService`

Likely future options:

- keep the package and split helpers into separate modules inside it
- keep `content-contract` pure and move helpers into another neighboring package
- move in-memory consumer helpers into app-local code if they are not truly shared

### TypeScript Documentation

When the TypeScript package model is ready, add a dedicated architecture document that covers:

- standard package categories
- dependency direction between packages
- what counts as contract code
- what counts as app code
- where helper behavior should live

## Next Backend Items

These remain better fits for the active backend track than the deferred TypeScript work:

- auth and session implementation
- media and asset workflow design
- publish and deployment integration
- content-repository operational behavior such as Git-managed working copies

## Notes

- Do not use this file to restate accepted architecture. Move settled decisions into `docs/decisions/` or `docs/explanation/`.
- Remove entries from this file when they become active branch work.
