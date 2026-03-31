# Backlog

## Purpose

Track deferred work that matters to the project but is not part of the current implementation focus.

This file is for backlog and sequencing. Durable product, architecture, and reference material belongs in `docs/`.

## Current Focus

- Spring Boot backend cleanup and backend feature work
- API contract clarity
- content-repository runtime behavior

## Deferred

### `packages/content-contract`

Status: resolved on the active cleanup branch

Decision:

- `packages/content-contract` contains shared content types and repository-shape constants only
- consumer helpers and runtime behavior do not belong in this package

Types that fit the current package well:

- `PostFrontmatter`
- `BlogPostDocument`
- `SiteSettings`
- `ContentRepositoryContract`
- `dictumContentContract`

Moved out of the package model:

- `BlogPostSummary`
- `BlogContentService`
- `estimateReadingMinutes`
- `summarizePost`
- `createInMemoryContentService`

### TypeScript Documentation

Status: resolved on the active cleanup branch

See:

- `docs/explanation/typescript-package-boundaries.md`

## Next Backend Items

These remain better fits for the active backend track than the deferred TypeScript work:

- auth and session implementation
- media and asset workflow design
- publish and deployment integration
- content-repository operational behavior such as Git-managed working copies

## Notes

- Do not use this file to restate accepted architecture. Move settled decisions into `docs/decisions/` or `docs/explanation/`.
- Remove entries from this file when they become active branch work.
