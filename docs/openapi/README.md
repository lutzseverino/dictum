---
title: OpenAPI
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-16
owner: Engineering
doc-type: index
summary: Define the location and purpose of machine-readable HTTP API contracts for Dictum.
---

# OpenAPI

## Purpose

Store machine-readable HTTP contract definitions for Dictum so API behavior can be reviewed, versioned, and discussed independently from backend implementation details.

## Documents

- [Dictum REST contract](./dictum.yaml)

## Scope Boundaries

- This area is for OpenAPI contract files and closely related notes.
- Human-readable explanations of API design belong in [Explanation](../explanation/README.md) or [Decision Records](../decisions/README.md).

## Current Scope

The first contract slice defines conventional REST endpoints for:

- posts
- site settings

This slice intentionally defines control-plane resources only. It does not standardize public rendering, theme delivery, or blog-frontend ownership.

It also intentionally does not standardize provider job resources or runtime auth implementation yet.
