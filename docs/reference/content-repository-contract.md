---
title: Content Repository Contract
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-12
owner: Engineering
doc-type: reference
summary: Define the expected structure of the external dictum-content repository.
---

# Content Repository Contract

## Purpose

Define the file and folder contract for the external `dictum-content` repository that will eventually act as the source of truth for site settings and markdown posts.

## Specification

The `dictum` application repository does not own content files directly. It expects a separate repository with this shape:

```text
posts/
  <slug>/
    index.md
    style.css        # optional
    meta.json        # optional
settings/
  site.json
```

## Rules

- `index.md` is the canonical post body.
- `style.css` is optional and applies only to a single post.
- `meta.json` is optional for post-specific metadata that should not live in frontmatter.
- `settings/site.json` contains site-level fields such as title, subtitle, and MOTD.

## Notes

- This repository contract is documented only in the current slice.
- Dictum does not yet clone, mount, or mutate the external content repository.

