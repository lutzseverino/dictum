# Backlog

## Purpose

Track deferred work that matters to the project but is not part of the current implementation focus.

This file is for backlog and sequencing. Durable product, architecture, and reference material belongs in `docs/`.

## Current Focus

- Spring Boot backend feature and infrastructure work
- API contract clarity
- content-repository runtime behavior

## Deferred

### `packages/content-contract`

Status: resolved

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

Status: resolved

See:

- `docs/explanation/typescript-package-boundaries.md`

## Next Backend Items

These remain better fits for the active backend track than the deferred TypeScript work.

### Error Semantics and Frontend Localization Contract

Status: active

Goal:

- keep backend error meaning stable without making English backend strings the UI contract

What to do:

- add stable machine-readable error codes to Problem Details responses
- add structured params for message interpolation where needed
- keep `type` as the canonical semantic identifier
- treat `title` and `detail` as fallback/debug strings rather than frontend source-of-truth copy
- document that frontend clients own localization of API-originated messages

Why it matters:

- full backend i18n can wait
- error-contract design should not wait, because raw backend strings become expensive contract debt once clients depend on them

### Auth and Session Implementation

Status: pending

- auth and session implementation

Goal:

- add real access control to the control-plane API and admin flows

### Media and Asset Workflow

Status: pending

- define how posts reference uploaded assets
- define backend storage behavior for media and other post-owned files

### Publish and Deployment Integration

Status: pending

- define how publish actions notify or trigger connected frontend deployments
- decide whether this uses hooks, events, or another explicit integration model

### Content-Repository Operations

Status: pending

- define operational behavior around filesystem-backed repositories
- evaluate later support for Git-managed working copies

### Operational Backend Hardening

Status: pending

- improve health/readiness behavior
- tighten startup and runtime failure reporting for invalid content repositories
- harden operational feedback around configuration and persistence boundaries

## Notes

- Do not use this file to restate accepted architecture. Move settled decisions into `docs/decisions/` or `docs/explanation/`.
- Remove entries from this file when they become active branch work.
