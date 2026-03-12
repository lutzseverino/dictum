import type { BlogPostSummary } from "@dictum/rendering";
import type { ReactNode } from "react";

export function ShellFrame(props: {
  eyebrow: string;
  title: string;
  subtitle: string;
  children: ReactNode;
}) {
  const { eyebrow, title, subtitle, children } = props;

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(251,191,36,0.18),_transparent_35%),linear-gradient(180deg,_#fffdf6_0%,_#f7f0df_100%)] text-stone-900">
      <div className="mx-auto flex min-h-screen w-full max-w-6xl flex-col gap-10 px-6 py-10 sm:px-10 lg:px-12">
        <header className="max-w-3xl space-y-4">
          <p className="text-xs font-semibold uppercase tracking-[0.35em] text-stone-500">
            {eyebrow}
          </p>
          <div className="space-y-3">
            <h1 className="font-serif text-5xl tracking-tight text-stone-950 sm:text-6xl">
              {title}
            </h1>
            <p className="max-w-2xl text-lg leading-8 text-stone-700 sm:text-xl">
              {subtitle}
            </p>
          </div>
        </header>
        {children}
      </div>
    </main>
  );
}

export function SurfacePanel(props: {
  title: string;
  kicker?: string;
  children: ReactNode;
}) {
  const { title, kicker, children } = props;

  return (
    <section className="rounded-[2rem] border border-stone-900/10 bg-white/80 p-6 shadow-[0_24px_80px_rgba(68,40,14,0.08)] backdrop-blur">
      <div className="mb-5 space-y-2">
        {kicker ? (
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-amber-700">
            {kicker}
          </p>
        ) : null}
        <h2 className="font-serif text-2xl text-stone-950">{title}</h2>
      </div>
      {children}
    </section>
  );
}

export function StatusPill(props: { children: ReactNode }) {
  return (
    <span className="inline-flex items-center rounded-full bg-stone-950 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-stone-50">
      {props.children}
    </span>
  );
}

export function PostCard(props: { post: BlogPostSummary }) {
  const { post } = props;

  return (
    <article className="flex h-full flex-col justify-between rounded-[1.75rem] border border-stone-900/10 bg-stone-50 p-5 transition-transform duration-200 hover:-translate-y-1">
      <div className="space-y-4">
        <div className="flex flex-wrap items-center gap-3">
          <StatusPill>{post.status}</StatusPill>
          <span className="text-sm text-stone-500">
            {post.template} · {post.readingMinutes} min
          </span>
        </div>
        <div className="space-y-2">
          <h3 className="font-serif text-2xl text-stone-950">{post.title}</h3>
          <p className="text-sm leading-7 text-stone-700">{post.excerpt}</p>
        </div>
      </div>
      <div className="mt-6 flex flex-wrap gap-2 text-xs uppercase tracking-[0.22em] text-stone-500">
        {post.tags.map((tag) => (
          <span key={tag}>{tag}</span>
        ))}
      </div>
    </article>
  );
}
