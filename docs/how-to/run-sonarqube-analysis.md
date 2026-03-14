---
title: Run SonarQube Analysis
status: accepted
author: Codex
created: 2026-03-14
updated: 2026-03-15
owner: Engineering
doc-type: how-to
summary: Start a local SonarQube Community Build instance and run the repository scan from the repo root.
---

# Run SonarQube Analysis

## When To Use This Guide

Use this guide when you want Dictum's existing build validations plus, when available, a SonarQube analysis against the whole monorepo.

## Prerequisites

- Java 21 and pnpm are available locally.
- If you want to run a local SonarQube server, Docker Desktop or another local Docker engine is running.
- If you want to run a local SonarQube server, you can access `http://localhost:9000`.

## Steps

1. If you want a local SonarQube server, run `pnpm sonar:start`.
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
  - a SonarQube scan that waits for the quality gate result when `SONAR_HOST_URL` and `SONAR_TOKEN` are configured
- If `SONAR_HOST_URL` or `SONAR_TOKEN` is missing, `pnpm sonar:validate` warns and skips only the SonarQube scan.
- `pnpm sonar:scan` runs only the scanner if the repo is already prepared.
- `pnpm sonar:stop` shuts down the local SonarQube stack.
- The repository root `sonar-project.properties` is the canonical scan configuration.

## GitHub Actions

- The repository includes a `SonarQube` workflow for `master`.
- It runs only when the repository variable `SONAR_HOST_URL` and the secret `SONAR_TOKEN` are configured.
- GitHub-hosted runners must be able to reach the SonarQube server over the network.
- `http://localhost:9000` works for local scans on your machine, but not for GitHub-hosted runners.
- If you want GitHub Actions to scan against a self-hosted Community Build instance, use either:
  - a network-reachable SonarQube host, or
  - a self-hosted GitHub runner that can reach the local SonarQube instance

## Verification

- `pnpm sonar:validate` exits successfully.
- If SonarQube is configured, the SonarQube project dashboard shows a completed analysis for `dictum`.
- If SonarQube is not configured, the command prints a warning that the scan was skipped after the standard validations completed.
