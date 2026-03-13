import type { SiteSettingsResponse } from "@dictum/api-client";
import { adminControlPlane } from "@/lib/control-plane";

export default function Home() {
  const previewSettings: SiteSettingsResponse = {
    title: "Dictum",
    subtitle: "A remotely steerable markdown blog kit.",
    motd: "Foundation mode is live: boundaries first, resources next.",
  };

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(2,132,199,0.16),_transparent_38%),linear-gradient(180deg,_#f8fafc_0%,_#f1f5f9_100%)] px-5 py-8 text-slate-950 sm:px-8">
      <div className="mx-auto flex max-w-md flex-col gap-5">
        <header className="space-y-3">
          <p className="text-xs font-semibold uppercase tracking-[0.34em] text-sky-700">
            Dictum Admin
          </p>
          <h1 className="text-4xl font-semibold tracking-tight">
            Phone-first controls, scaffolded.
          </h1>
          <p className="text-base leading-7 text-slate-600">
            This shell is reserved for future post publishing, subtitle changes,
            MOTD updates, and provider-assisted requests.
          </p>
        </header>

        <section className="rounded-[1.75rem] border border-slate-900/10 bg-white/90 p-5 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
            Route groups
          </p>
          <ul className="mt-4 space-y-3 text-sm text-slate-700">
            <li>/api/v1/posts</li>
            <li>/api/v1/settings/site</li>
          </ul>
        </section>

        <section className="rounded-[1.75rem] border border-slate-900/10 bg-slate-950 p-5 text-slate-50 shadow-[0_24px_80px_rgba(15,23,42,0.12)]">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-sky-300">
            Status
          </p>
          <div className="mt-4 grid gap-3 text-sm">
            <div className="rounded-2xl bg-white/8 p-4">
              Auth remains a declared contract boundary, but runtime enforcement
              is still deferred.
            </div>
            <div className="rounded-2xl bg-white/8 p-4">
              The generated TypeScript client now targets the same OpenAPI
              contract as the Spring backend.
            </div>
            <div className="rounded-2xl bg-white/8 p-4">
              The admin app can now grow around typed `posts` and `siteSettings`
              clients.
            </div>
          </div>
        </section>

        <section className="rounded-[1.75rem] border border-slate-900/10 bg-white/90 p-5 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
            Generated preview
          </p>
          <div className="mt-4 space-y-3 text-sm text-slate-700">
            <p>
              Typed clients available:{" "}
              {adminControlPlane.posts.constructor.name} and{" "}
              {adminControlPlane.siteSettings.constructor.name}
            </p>
            <p>{previewSettings.title}</p>
            <p>{previewSettings.subtitle}</p>
            <p>{previewSettings.motd}</p>
          </div>
        </section>
      </div>
    </main>
  );
}
