import { dictumContentContract } from "@dictum/rendering";
import {
  PostCard,
  ShellFrame,
  StatusPill,
  SurfacePanel,
} from "@dictum/site-kit";
import { contentService } from "@/lib/content-service";

export default async function Home() {
  const [settings, posts] = await Promise.all([
    contentService.getSiteSettings(),
    contentService.listPosts(),
  ]);

  return (
    <ShellFrame
      eyebrow="Public Blog Shell"
      title={settings.title}
      subtitle={settings.subtitle}
    >
      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.7fr)_minmax(20rem,1fr)]">
        <SurfacePanel title="Message of the day" kicker="Settings contract">
          <div className="space-y-4">
            <p className="text-lg leading-8 text-stone-700">{settings.motd}</p>
            <p className="text-sm leading-7 text-stone-500">
              The site reads settings through a content service abstraction,
              ready for a future external repository named{" "}
              <span className="font-semibold text-stone-800">
                {dictumContentContract.repositoryName}
              </span>
              .
            </p>
          </div>
        </SurfacePanel>
        <SurfacePanel title="Current boundaries" kicker="Skeleton scope">
          <ul className="space-y-3 text-sm leading-7 text-stone-700">
            <li>Markdown stays canonical content.</li>
            <li>Per-post CSS remains a sidecar contract.</li>
            <li>
              Remote controls will arrive through the Spring control plane.
            </li>
          </ul>
        </SurfacePanel>
      </div>

      <SurfacePanel title="Placeholder posts" kicker="Content service">
        <div className="mb-6 flex flex-wrap gap-3">
          <StatusPill>{posts.length} seeded entries</StatusPill>
          <StatusPill>{dictumContentContract.postFileName}</StatusPill>
          <StatusPill>{dictumContentContract.optionalPostStyleFile}</StatusPill>
        </div>
        <div className="grid gap-5 md:grid-cols-2">
          {posts.map((post) => (
            <PostCard key={post.slug} post={post} />
          ))}
        </div>
      </SurfacePanel>
    </ShellFrame>
  );
}
