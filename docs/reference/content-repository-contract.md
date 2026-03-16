---
title: Content Repository Contract
status: accepted
author: Codex
created: 2026-03-12
updated: 2026-03-15
owner: Engineering
doc-type: reference
summary: Define the expected structure of the external dictum-content repository.
---

# Content Repository Contract

Content lives in a separate `dictum-content` repository with this shape:

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

- `index.md` is the canonical post body and stores YAML frontmatter plus markdown content.
- `style.css` is optional and applies only to a single post.
- `meta.json` is optional for post-specific metadata that should not live in frontmatter.
- `settings/site.json` contains site-level fields such as title, subtitle, and MOTD.

## Post Frontmatter

Each `posts/<slug>/index.md` file starts with YAML frontmatter using these fields:

```yaml
title: Dictum Begins
slug: dictum-begins
excerpt: A first post for a Dictum-managed blog.
publishedAt: 2026-03-12 # optional for drafts
tags:
  - architecture
  - foundation
template: essay
status: published
```

The markdown body begins after the closing `---` frontmatter boundary.
