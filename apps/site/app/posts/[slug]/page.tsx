import { ShellFrame, StatusPill, SurfacePanel } from "@dictum/site-kit";
import { notFound } from "next/navigation";
import { contentService } from "@/lib/content-service";

export async function generateStaticParams() {
  const posts = await contentService.listPosts();
  return posts.map((post) => ({ slug: post.slug }));
}

export default async function PostPage(props: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await props.params;
  const [settings, post] = await Promise.all([
    contentService.getSiteSettings(),
    contentService.getPost(slug),
  ]);

  if (!post) {
    notFound();
  }

  return (
    <ShellFrame
      eyebrow="Post contract"
      title={post.frontmatter.title}
      subtitle={settings.subtitle}
    >
      <SurfacePanel title="Frontmatter" kicker={post.frontmatter.template}>
        <div className="flex flex-wrap gap-3">
          <StatusPill>{post.frontmatter.status}</StatusPill>
          <StatusPill>{post.contentPath}</StatusPill>
          {post.stylesheets.map((stylesheet) => (
            <StatusPill key={stylesheet.href}>{stylesheet.href}</StatusPill>
          ))}
        </div>
      </SurfacePanel>

      <SurfacePanel title="Body preview" kicker="Markdown boundary">
        <article className="max-w-3xl space-y-6 text-lg leading-8 text-stone-700">
          {post.body.split("\n\n").map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </article>
      </SurfacePanel>
    </ShellFrame>
  );
}
