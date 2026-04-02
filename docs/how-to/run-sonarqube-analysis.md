---
title: Run SonarQube Analysis
status: accepted
author: Codex
created: 2026-03-14
updated: 2026-04-02
owner: Engineering
doc-type: how-to
summary: Start a local SonarQube Community Build instance, run the repository scan, and inspect IDE-backed local diagnostics.
---

# Run SonarQube Analysis

## When To Use This Guide

Use this guide when you want Dictum's existing build validations, a SonarQube server analysis against the whole monorepo, or local IDE-backed Sonar diagnostics for specific files and folders.

## Prerequisites

- Java 21 and pnpm are available locally.
- If you want to run a local SonarQube server, Docker Desktop or another local Docker engine is running.
- If you want to run a local SonarQube server, you can access `http://localhost:9000`.
- If you want IDE-backed local diagnostics, open this repository in VS Code with the SonarQube for IDE extension enabled.

## Steps

1. Start SonarQube explicitly with `pnpm sonar:start` if you want to use the local Docker-based server.
2. Create a SonarQube token.
3. Configure `SONAR_HOST_URL` and `SONAR_TOKEN`, or store them in `~/.config/dictum/sonarqube.json`.
4. Run `pnpm sonar:validate`.
5. If you want to target a specific file or folder, pass standard Sonar scanner properties after `--`.

## Inspect Local IDE Diagnostics

1. Open this repository in VS Code with the SonarQube for IDE extension enabled.
2. Run `pnpm sonar:ide <path ...>` from the repo root.
3. Pass one or more files or folders. If you omit all paths, the command analyzes the current working directory.
4. If automatic IDE bridge detection chooses the wrong VS Code window, rerun with `SONARQUBE_IDE_PORT=<port>`.

## Notes

- `pnpm sonar:validate` runs:
  - OpenAPI code generation
  - frontend linting and typechecking
  - Maven `verify` for the API, including JaCoCo XML coverage generation
  - a SonarQube scan that waits for the quality gate result when `SONAR_HOST_URL` and `SONAR_TOKEN` are configured
- `pnpm sonar:ide` runs local SonarQube for IDE diagnostics through the official SonarQube MCP bridge.
- `pnpm sonar:validate` and `pnpm sonar:ide` do not start Docker automatically unless you set `DICTUM_SONAR_AUTO_START=true`.
- If you still want an auto-started local Docker stack to shut down after server validation, set `DICTUM_SONAR_STOP_AFTER_VALIDATE=true`.
- The local config file path is:
  - `$XDG_CONFIG_HOME/dictum/sonarqube.json`, or
  - `~/.config/dictum/sonarqube.json` when `XDG_CONFIG_HOME` is not set
- If `SONAR_HOST_URL` or `SONAR_TOKEN` is missing, `pnpm sonar:validate` warns and skips only the SonarQube scan.
- If `SONAR_HOST_URL` or `SONAR_TOKEN` is missing, `pnpm sonar:ide` warns that IDE-backed diagnostics were not used.
- `pnpm sonar:validate` prints phase headers plus diagnostics instead of the full successful tool output.
- `pnpm sonar:validate -- ...` forwards additional arguments to the Sonar scanner.
- To target a specific folder or file, prefer standard scanner properties such as:
  - `pnpm sonar:validate -- "-Dsonar.inclusions=services/api/src/main/java/dev/dictum/api/auth/**"`
  - `pnpm sonar:validate -- "-Dsonar.inclusions=services/api/src/main/java/dev/dictum/api/auth/controller/SessionController.java"`
- `pnpm sonar:ide` accepts one or more files or folders directly, for example:
  - `pnpm sonar:ide services/api/src/test/java/dev/dictum/api/support/FilesystemContentFixture.java`
  - `pnpm sonar:ide services/api/src/main/java/dev/dictum/api/web/error`
- `pnpm sonar:ide` uses the most recently active SonarQube for IDE bridge by default.
- If the wrong VS Code window is selected, set `SONARQUBE_IDE_PORT` explicitly.
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
- `pnpm sonar:ide <path ...>` prints local findings with severity, file path, and line range.
- If SonarQube is configured, the SonarQube project dashboard shows a completed analysis for `dictum`.
- If SonarQube is not configured, the command prints a warning that the scan was skipped after the standard validations completed.
