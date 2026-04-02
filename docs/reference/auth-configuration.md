---
title: Auth Configuration
status: accepted
author: Codex
created: 2026-04-01
updated: 2026-04-01
owner: Engineering
doc-type: reference
summary: Define the configuration required for Dictum's control-plane session authentication.
---

# Auth Configuration

## Properties

- `dictum.auth.admin.username`
- `dictum.auth.admin.password`

## Environment Variables

- `DICTUM_AUTH_ADMIN_USERNAME`
- `DICTUM_AUTH_ADMIN_PASSWORD`

## Requirements

- Both values are required for the API to start.
- The first auth slice supports one configured admin identity.
- Authenticated API sessions use the standard `JSESSIONID` cookie.

## Request Requirements

- `POST /api/v1/session` authenticates the configured admin identity.
- `GET /api/v1/session` returns the current authenticated session.
- `DELETE /api/v1/session` invalidates the current authenticated session.
- Unsafe authenticated API requests must include the `X-CSRF-TOKEN` header.
