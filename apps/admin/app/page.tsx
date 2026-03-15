import { PostList } from "@/components/post-list";
import { SiteSettingsForm } from "@/components/site-settings-form";
import { getAdminDashboardData } from "@/lib/control-plane/server";

export const dynamic = "force-dynamic";

export default async function Home() {
  const dashboard = await getAdminDashboardData();

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(2,132,199,0.16),_transparent_38%),linear-gradient(180deg,_#f8fafc_0%,_#f1f5f9_100%)] px-5 py-8 text-slate-950 sm:px-8">
      <div className="mx-auto flex max-w-md flex-col gap-5">
        <header className="space-y-3">
          <p className="text-xs font-semibold uppercase tracking-[0.34em] text-sky-700">
            Dictum Admin
          </p>
          <h1 className="text-4xl font-semibold tracking-tight">
            Phone-first controls, live.
          </h1>
          <p className="text-base leading-7 text-slate-600">
            The admin shell now reads real posts and site settings from the
            Spring control plane, with site-level edits flowing through the same
            API contract.
          </p>
        </header>

        <section className="rounded-[1.75rem] border border-slate-900/10 bg-slate-950 p-5 text-slate-50 shadow-[0_24px_80px_rgba(15,23,42,0.12)]">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-sky-300">
            Status
          </p>
          <div className="mt-4 grid gap-3 text-sm">
            <div className="rounded-2xl bg-white/8 p-4">
              Runtime auth is still deferred, so this app is currently a local
              integration shell rather than a protected admin console.
            </div>
            <div className="rounded-2xl bg-white/8 p-4">
              Reads and writes now run through a server-only wrapper around the
              generated OpenAPI client.
            </div>
            <div className="rounded-2xl bg-white/8 p-4">
              Post editing and publishing can arrive next without redesigning
              the control-plane boundary.
            </div>
          </div>
        </section>

        {dashboard.problem ? (
          <section className="rounded-[1.75rem] border border-rose-200 bg-rose-50/90 p-5 shadow-[0_24px_80px_rgba(190,24,93,0.08)] backdrop-blur">
            <p className="text-xs font-semibold uppercase tracking-[0.3em] text-rose-700">
              Control plane unavailable
            </p>
            <div className="mt-4 space-y-3">
              <p className="text-lg font-semibold text-rose-950">
                {dashboard.problem.title}
              </p>
              <p className="text-sm leading-7 text-rose-800">
                {dashboard.problem.detail}
              </p>
              {dashboard.problem.status ? (
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-rose-700">
                  HTTP {dashboard.problem.status}
                </p>
              ) : null}
            </div>
          </section>
        ) : (
          <>
            <section className="rounded-[1.75rem] border border-slate-900/10 bg-white/90 p-5 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur">
              <div className="space-y-2">
                <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
                  Site settings
                </p>
                <h2 className="text-2xl font-semibold tracking-tight text-slate-950">
                  Update the homepage frame
                </h2>
                <p className="text-sm leading-6 text-slate-600">
                  This first writable slice targets title, subtitle, and MOTD
                  through the existing PATCH endpoint.
                </p>
              </div>
              <div className="mt-5">
                <SiteSettingsForm settings={dashboard.siteSettings} />
              </div>
            </section>

            <section className="rounded-[1.75rem] border border-slate-900/10 bg-white/90 p-5 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur">
              <div className="space-y-2">
                <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
                  Posts
                </p>
                <h2 className="text-2xl font-semibold tracking-tight text-slate-950">
                  Read-side control plane
                </h2>
                <p className="text-sm leading-6 text-slate-600">
                  Post mutation flows can layer on top of this real list in the
                  next admin slice.
                </p>
              </div>
              <div className="mt-5">
                <PostList posts={dashboard.posts} />
              </div>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
