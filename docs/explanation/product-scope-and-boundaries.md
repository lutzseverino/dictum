---
title: Product Scope and Boundaries
status: accepted
author: Codex
created: 2026-03-16
updated: 2026-03-16
owner: Engineering
doc-type: explanation
summary: Define Dictum's product boundary as a control plane for developer-owned personal blogs.
---

# Product Scope and Boundaries

Dictum is an open-source control plane for developer-owned personal blogs.

It manages publishing operations and blog configuration. It does not own the public presentation layer.

## Core Surface

Dictum includes:

- a control-plane HTTP API for blog operations such as post management and site settings
- a mobile-first admin web app for managing those operations
- shared contracts and client packages that let admin and external consumers speak to the same API cleanly
- content-repository contracts for markdown-first blog workflows

## External Consumer Surface

The public blog frontend is an external consumer of Dictum.

Typical shapes include:

- build a custom blog in a separate repository
- self-host that frontend independently
- connect it to a Dictum deployment through the API and content contracts

## Hosting Model

Dictum supports self-hosting.

A hosted Dictum offering fits this product boundary by operating:

- the Dictum control plane
- the Dictum admin app
- connected blog frontends authored in separate repositories

## Public Frontends

Public-site experiments, starters, and blog-specific UI packages live outside Dictum's required product surface.

They may live in:

- separate blog repositories
- example repositories
- non-core example directories

## Non-Goals

- owning the public blog frontend
- shipping a canonical blog theme
- expanding into newsletter, membership, or publication-suite features
- becoming a generic CMS for arbitrary content domains
