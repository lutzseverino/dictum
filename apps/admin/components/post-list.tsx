import type { PostSummary } from "@dictum/api-client";

function formatPublishedAt(value?: Date | null) {
  if (!value) {
    return "Draft";
  }

  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(value);
}

export function PostList(props: { posts: PostSummary[] }) {
  return (
    <div className="space-y-3">
      {props.posts.map((post) => (
        <article
          className="rounded-[1.4rem] border border-slate-900/10 bg-slate-50/80 p-4"
          key={post.slug}
        >
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-2">
              <p className="text-lg font-semibold text-slate-950">
                {post.title}
              </p>
              <p className="text-sm text-slate-500">{post.slug}</p>
            </div>
            <span className="rounded-full border border-slate-900/10 bg-white px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-slate-600">
              {post.status}
            </span>
          </div>

          <p className="mt-3 text-sm leading-6 text-slate-700">
            {post.excerpt}
          </p>

          <div className="mt-4 flex flex-wrap gap-2 text-xs text-slate-600">
            <span className="rounded-full bg-white px-3 py-1">
              {post.template}
            </span>
            <span className="rounded-full bg-white px-3 py-1">
              {formatPublishedAt(post.publishedAt)}
            </span>
            <span className="rounded-full bg-white px-3 py-1">
              {post.hasStylesheet ? "stylesheet" : "no stylesheet"}
            </span>
            {post.tags.map((tag) => (
              <span className="rounded-full bg-white px-3 py-1" key={tag}>
                #{tag}
              </span>
            ))}
          </div>
        </article>
      ))}
    </div>
  );
}
