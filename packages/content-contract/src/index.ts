export type PostStatus = "draft" | "published";
export type PostTemplate = "essay" | "note" | "dispatch";

export interface PostStylesheet {
  href: string;
  scope: "post";
}

export interface PostFrontmatter {
  title: string;
  slug: string;
  excerpt: string;
  publishedAt?: string;
  tags: string[];
  template: PostTemplate;
  status: PostStatus;
}

export interface BlogPostDocument {
  frontmatter: PostFrontmatter;
  body: string;
  stylesheets: PostStylesheet[];
}

export interface SiteSettings {
  title: string;
  subtitle: string;
  motd: string;
}

export interface ContentRepositoryContract {
  postsRoot: "posts";
  settingsFile: "settings/site.json";
  postFileName: "index.md";
  optionalPostStyleFile: "style.css";
  optionalPostMetaFile: "meta.json";
}

export const dictumContentContract: ContentRepositoryContract = {
  postsRoot: "posts",
  settingsFile: "settings/site.json",
  postFileName: "index.md",
  optionalPostStyleFile: "style.css",
  optionalPostMetaFile: "meta.json",
};
