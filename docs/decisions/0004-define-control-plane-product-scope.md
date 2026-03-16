---
title: Define control-plane product scope
status: accepted
author: Codex
created: 2026-03-16
updated: 2026-03-16
owner: Engineering
doc-type: decision
summary: Define Dictum as a control plane for developer-owned personal blogs rather than a product that owns the public blog frontend.
---

# Define control-plane product scope

## Status

Accepted

## Context

Dictum originated from the desire to separate the blog-management concerns of a personal website from the website's public presentation layer.

The repository currently contains both a control-plane backend and placeholder public-site scaffolding. Without an explicit product-scope decision, contributors can reasonably read Dictum as a general blog platform that intends to own the public frontend, theme layer, and other publication-platform concerns.

That ambiguity makes roadmap decisions harder and increases the risk of accidental scope expansion.

## Decision

Dictum is defined as an open-source control plane for developer-owned personal blogs.

Dictum's core product surface includes:

- the publishing and management API
- the admin dashboard
- shared API clients and content contracts needed to support those surfaces

Dictum does not treat the public blog frontend as part of its required product surface.

Public-site implementations are external consumers that may live in separate repositories and connect to Dictum through its API and shared contracts. Experimental public-site shells or UI packages may remain in the repository temporarily, but they are not roadmap-defining product commitments.

## Consequences

- The project gains a clearer product identity.
- Scope decisions can be evaluated against a narrower control-plane boundary.
- Public-site design and branding remain separate from Dictum's product commitments.
- Future managed hosting work can focus on operating the control plane and connected frontends rather than redefining the product boundary.

## Alternatives Considered

- Treat Dictum as a full blog platform that owns both management and public frontend concerns
  Rejected because it expands the product surface too early and weakens the separation that motivated the project in the first place.
- Treat Dictum as a generic CMS for arbitrary content domains
  Rejected because the current value proposition is specifically about personal blog operations, not a broad content-management platform.
