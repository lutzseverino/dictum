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

1. Run `pnpm sonar:validate`.
2. If SonarQube is not configured and the terminal is interactive, the script can offer to start the local Docker-based SonarQube stack automatically.
3. If you accept the local setup, wait for SonarQube to become reachable at `http://localhost:9000`.
4. Create a SonarQube token and paste it when the script asks for one.
5. The script stores the local SonarQube host and token in your user config so future runs do not need the same prompt.

## Notes

- `pnpm sonar:validate` runs:
  - OpenAPI code generation
  - frontend linting and typechecking
  - Maven `verify` for the API, including JaCoCo XML coverage generation
  - a SonarQube scan that waits for the quality gate result when `SONAR_HOST_URL` and `SONAR_TOKEN` are configured
- If the Sonar env vars are missing but the terminal is interactive, the script can offer local Docker-based setup automatically.
- If you provide a token during the local setup flow, the script stores it in:
  - `$XDG_CONFIG_HOME/dictum/sonarqube.json`, or
  - `~/.config/dictum/sonarqube.json` when `XDG_CONFIG_HOME` is not set
- When local SonarQube is configured for `http://localhost:9000`, the script also tries to start the local Docker stack automatically if the server is not already running.
- If `SONAR_HOST_URL` or `SONAR_TOKEN` is missing, `pnpm sonar:validate` warns and skips only the SonarQube scan.
- `pnpm sonar:scan` runs only the scanner if the repo is already prepared.
- `pnpm sonar:stop` shuts down the local SonarQube stack.
- The repository root `sonar-project.properties` is the canonical scan configuration.

## GitHub Actions

- The repository includes a `SonarQube` workflow for `master`.
- It skips the scan steps when the repository variable `SONAR_HOST_URL` and the secret `SONAR_TOKEN` are not configured.
- GitHub-hosted runners must be able to reach the SonarQube server over the network.
- `http://localhost:9000` works for local scans on your machine, but not for GitHub-hosted runners.
- If you want GitHub Actions to scan against a self-hosted Community Build instance, use either:
  - a network-reachable SonarQube host, or
  - a self-hosted GitHub runner that can reach the local SonarQube instance

## Verification

- `pnpm sonar:validate` exits successfully.
- If SonarQube is configured, the SonarQube project dashboard shows a completed analysis for `dictum`.
- If SonarQube is not configured, the command prints a warning that the scan was skipped after the standard validations completed.
