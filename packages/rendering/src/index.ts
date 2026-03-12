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
  contentPath: string;
  metaPath?: string;
}

export interface BlogPostSummary extends PostFrontmatter {
  readingMinutes: number;
}

export interface SiteSettings {
  title: string;
  subtitle: string;
  motd: string;
}

export interface BlogContentService {
  getSiteSettings(): Promise<SiteSettings>;
  listPosts(): Promise<BlogPostSummary[]>;
  getPost(slug: string): Promise<BlogPostDocument | null>;
}

export interface ContentRepositoryContract {
  repositoryName: "dictum-content";
  postsRoot: "posts";
  settingsFile: "settings/site.json";
  postFileName: "index.md";
  optionalPostStyleFile: "style.css";
  optionalPostMetaFile: "meta.json";
}

export const dictumContentContract: ContentRepositoryContract = {
  repositoryName: "dictum-content",
  postsRoot: "posts",
  settingsFile: "settings/site.json",
  postFileName: "index.md",
  optionalPostStyleFile: "style.css",
  optionalPostMetaFile: "meta.json",
};

export function estimateReadingMinutes(markdown: string): number {
  const words = markdown.trim().split(/\s+/).filter(Boolean).length;
  return Math.max(1, Math.ceil(words / 220));
}

export function summarizePost(post: BlogPostDocument): BlogPostSummary {
  return {
    ...post.frontmatter,
    readingMinutes: estimateReadingMinutes(post.body),
  };
}

export function createInMemoryContentService(seed: {
  settings: SiteSettings;
  posts: BlogPostDocument[];
}): BlogContentService {
  return {
    async getSiteSettings() {
      return seed.settings;
    },
    async listPosts() {
      return seed.posts.map(summarizePost);
    },
    async getPost(slug: string) {
      return seed.posts.find((post) => post.frontmatter.slug === slug) ?? null;
    },
  };
}
