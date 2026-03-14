---
title: Run SonarQube Analysis
status: accepted
author: Codex
created: 2026-03-14
updated: 2026-03-14
owner: Engineering
doc-type: how-to
summary: Start a local SonarQube Community Build instance and run the repository scan from the repo root.
---

# Run SonarQube Analysis

## When To Use This Guide

Use this guide when you want Dictum's existing build validations plus a SonarQube analysis against the whole monorepo.

## Prerequisites

- Docker Desktop or another local Docker engine is running.
- Java 21 and pnpm are available locally.
- You can access `http://localhost:9000`.

## Steps

1. From the repository root, run `pnpm sonar:start`.
2. Open `http://localhost:9000` and sign in with the default SonarQube credentials `admin` / `admin`.
3. Change the default password when prompted.
4. Create a user token from your account security settings and export it as `SONAR_TOKEN`.
5. Export `SONAR_HOST_URL=http://localhost:9000`.
6. Run `pnpm sonar:validate`.

## Notes

- `pnpm sonar:validate` runs:
  - OpenAPI code generation
  - frontend linting and typechecking
  - Maven `verify` for the API, including JaCoCo XML coverage generation
  - a SonarQube scan that waits for the quality gate result
- `pnpm sonar:scan` runs only the scanner if the repo is already prepared.
- `pnpm sonar:stop` shuts down the local SonarQube stack.
- The repository root `sonar-project.properties` is the canonical scan configuration.

## GitHub Actions

- The repository includes a `SonarQube` workflow for `master`.
- It runs only when the repository variable `SONAR_HOST_URL` and the secret `SONAR_TOKEN` are configured.
- For a self-hosted Community Build setup, point `SONAR_HOST_URL` at the reachable SonarQube server.

## Verification

- `pnpm sonar:validate` exits successfully.
- The SonarQube project dashboard shows a completed analysis for `dictum`.
