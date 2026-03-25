---
title: Documentation Templates
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-25
owner: Engineering
doc-type: index
summary: Provide starting points for Dictum documentation while preserving doc-type-specific writing rules.
---

# Documentation Templates

Use these templates to preserve Dictum's documentation structure without forcing every doc into the same tone.

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

## Writing Rules By Doc Type

- `index`: short directory or section map; include purpose and scope notes when the section boundary would otherwise be ambiguous
- `reference`: factual, contract-first, no rationale unless absolutely required for interpretation
- `how-to`: task-first instructions and verification
- `tutorial`: guided learning sequence for a full outcome
- `explanation`: stable conceptual guidance; avoid proposal or checklist framing
- `decision`: MADR decision record with context, decision, consequences, and alternatives

## Usage Rules

1. Copy the template into the target folder.
2. Replace all placeholder values before committing.
3. Remove instructional notes that do not belong in the final document.
4. Link the new document from the relevant section index.
