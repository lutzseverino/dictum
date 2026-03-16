---
title: Product Scope and Boundaries
status: accepted
author: Codex
created: 2026-03-16
updated: 2026-03-16
owner: Engineering
doc-type: explanation
summary: Explain Dictum's product definition as a control plane for developer-owned personal blogs and clarify which repo areas are core versus transitional.
---

# Product Scope and Boundaries

## Problem

Dictum started with a broad "blog platform" framing that left room for multiple interpretations. The presence of a public-site app shell and public-site UI package made it easy to read the repository as if Dictum were trying to own the blog frontend itself.

That ambiguity creates roadmap risk. A product that owns the API, admin dashboard, public frontend, themes, and hosting at once will accumulate scope faster than the current project can support safely.

## Explanation

Dictum is an open-source control plane for developer-owned personal blogs.

Its job is to manage publishing operations, not to own the public presentation layer.

### Core Product Surface

Dictum's intended core surface is:

- a control-plane HTTP API for blog operations such as post management and site settings
- a mobile-first admin web app for managing those operations
- shared contracts and client packages that let admin and external consumers speak to the same API cleanly
- content-repository contracts for markdown-first blog workflows

### External Consumer Surface

The public blog frontend is an external consumer of Dictum, not part of Dictum's required product surface.

That means a user may:

- build a custom blog in a separate repository
- self-host that frontend independently
- connect it to a self-hosted or managed Dictum deployment

### Hosting Model

Dictum may later monetize by offering managed hosting for:

- the Dictum control plane
- the Dictum admin app
- connected blog frontends that users author in their own repositories

This hosting story does not change the product boundary. Managed hosting is an operational offering built around the control plane, not a reason for Dictum to absorb public-site ownership into the core repository.

### Public-Site Work

Public-site experiments, starters, and blog-specific UI packages should live outside Dictum's required product surface.

They may live in:

- separate blog repositories
- example repositories
- future example directories that are explicitly non-core

They should not shape the control-plane roadmap or weaken the separation between Dictum and each blog's public implementation.

## Consequences

- Dictum can stay focused on being excellent at publishing operations for personal blogs
- public-site design decisions remain fully owned by each blog implementation
- the repo becomes easier to understand because "core product" and "consumer example" stop blending together
- comparisons against projects such as Ghost become clearer because Dictum is choosing a narrower problem
