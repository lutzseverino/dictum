---
title: Documentation Templates
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-12
owner: Engineering
doc-type: index
summary: Explain how Dictum documentation templates map to Diataxis and MADR.
---

# Documentation Templates

## Purpose

Provide consistent starting points for new documentation without forcing contributors to invent structure from scratch.

## Available Templates

- [index.template.md](./index.template.md)
  Use for a section index such as `docs/reference/README.md`
- [tutorial.template.md](./tutorial.template.md)
  Use for a teaching-oriented walkthrough
- [how-to.template.md](./how-to.template.md)
  Use for a task-focused guide
- [reference.template.md](./reference.template.md)
  Use for contracts, schemas, and factual lookup material
- [explanation.template.md](./explanation.template.md)
  Use for architectural rationale and conceptual guidance
- [decision.template.md](./decision.template.md)
  Use for MADR-style decision records

## Usage Rules

1. Copy the template into the target folder.
2. Replace all placeholder values before committing.
3. Remove instructional notes that do not belong in the final document.
4. Link the new document from the relevant section index.

