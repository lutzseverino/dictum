# Dictum Content Repository Contract

The `dictum` application repo does not own content files directly. It expects a separate `dictum-content` repository with this shape:

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

## Current Scope

This skeleton only documents the contract. It does not clone, mount, or mutate the external content repository yet.

