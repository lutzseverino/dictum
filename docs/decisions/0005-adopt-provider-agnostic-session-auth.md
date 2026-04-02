---
title: Adopt provider-agnostic session authentication
status: accepted
author: Codex
created: 2026-04-01
updated: 2026-04-01
owner: Engineering
doc-type: decision
summary: Define Dictum's control-plane authentication boundary around authenticated sessions while keeping identity verification provider-agnostic.
---

# Adopt provider-agnostic session authentication

## Status

Accepted

## Context

Dictum's control-plane API now has a stable error contract and a clearer backend boundary model, but it still lacks authenticated access to admin capabilities.

The project needs a first auth slice that is simple enough to build now and stable enough to extend later. In particular, the first implementation should not force a rewrite if Dictum later adds OAuth or another external identity provider.

## Decision

Dictum will define authentication around authenticated control-plane sessions.

The durable backend boundary is:

- protected control-plane endpoints require an authenticated session
- authorization rules depend on the authenticated session, not on the upstream identity provider
- backend code outside the auth boundary should not depend on password-specific or OAuth-specific concepts

The first implementation will use:

- one configured admin identity
- local credential verification
- cookie-backed session authentication
- session-oriented API behavior for login, logout, and current-session lookup

OAuth is not part of the first auth slice, but the auth boundary must remain compatible with adding OAuth later as another way to establish the same Dictum session model.

## Consequences

- The first auth slice stays small and suitable for a single-operator control plane.
- Route protection, authorization, and current-session lookup can remain stable if new identity providers are added later.
- The project avoids making local credentials the core application auth model.
- OAuth, multi-user support, and broader identity management remain follow-up work.

## Alternatives Considered

- Treat local username and password authentication as the permanent auth model
  Rejected because it would make the first implementation harder to extend cleanly if Dictum later adds OAuth or another provider.
- Implement OAuth as the first auth slice
  Rejected because it adds provider and callback complexity before Dictum has even established its own session and authorization model.
- Delay all auth design decisions until implementation starts
  Rejected because authentication is a core backend boundary and benefits from a durable decision before code is added.
