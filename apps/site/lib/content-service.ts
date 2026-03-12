import {
  type BlogPostDocument,
  createInMemoryContentService,
} from "@dictum/rendering";

const seededPosts: BlogPostDocument[] = [
  {
    frontmatter: {
      title: "Dictum Begins",
      slug: "dictum-begins",
      excerpt:
        "A first seeded document proving that the site reads through a content service instead of the filesystem.",
      publishedAt: "2026-03-12",
      tags: ["architecture", "skeleton"],
      template: "essay",
      status: "published",
    },
    body: `Dictum starts life as a hybrid stack with a deliberate split between content, control plane, and presentation.

The public site is already reading through a service abstraction so a future content repository can arrive without rewriting the shell.`,
    stylesheets: [{ href: "posts/dictum-begins/style.css", scope: "post" }],
    contentPath: "posts/dictum-begins/index.md",
    metaPath: "posts/dictum-begins/meta.json",
  },
  {
    frontmatter: {
      title: "Remote Controls, Later",
      slug: "remote-controls-later",
      excerpt:
        "The admin experience will eventually own publish, subtitle, and MOTD commands through the Spring control plane.",
      tags: ["admin", "control-plane"],
      template: "dispatch",
      status: "draft",
    },
    body: `The admin app is intentionally a separate shell because phone-driven operations deserve their own rhythm and constraints.

This first slice keeps that boundary visible without implementing live mutations yet.`,
    stylesheets: [],
    contentPath: "posts/remote-controls-later/index.md",
  },
];

export const contentService = createInMemoryContentService({
  settings: {
    title: "Dictum",
    subtitle: "A remotely steerable markdown blog kit.",
    motd: "Skeleton mode is live: boundaries first, mutations later.",
  },
  posts: seededPosts,
});
