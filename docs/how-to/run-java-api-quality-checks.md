---
title: Run Java API Quality Checks
status: accepted
author: Codex
created: 2026-03-13
updated: 2026-03-14
owner: Engineering
doc-type: how-to
summary: Run formatting, baseline validation, and tests for the Spring Boot API from the repo root.
---

# Run Java API Quality Checks

## When To Use This Guide

Use this guide when you are changing `services/api` and want to format Java sources, confirm the Java and Maven baseline, or run the API test suite.

## Steps

1. From the repository root, run `pnpm lint:api` to verify the Maven and Java version baseline and check formatting with Spotless.
2. If formatting is required, run `pnpm format:api` to apply Google Java Format through Spotless.
3. Run `pnpm test:api` to execute the Spring Boot API test suite.
4. If you need JaCoCo coverage output for SonarQube or CI verification, run `pnpm verify:api`.

## Notes

- `pnpm lint:api` runs Maven Enforcer and `spotless:check`.
- `pnpm format:api` runs `spotless:apply`.
- Formatting uses Google Java Format through the Spotless Maven plugin.
- `pnpm verify:api` runs the full Maven `verify` lifecycle and produces the JaCoCo XML report consumed by SonarQube.

## Verification

- `pnpm lint:api` exits successfully with no formatting violations.
- `pnpm test:api` exits successfully with all tests passing.
