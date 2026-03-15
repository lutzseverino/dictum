"use client";

import type { SiteSettingsResponse } from "@dictum/api-client";
import { useActionState } from "react";
import {
  type UpdateSiteSettingsActionState,
  updateSiteSettingsAction,
} from "@/app/actions";

function SubmitButton(props: { disabled: boolean }) {
  return (
    <button
      className="inline-flex min-h-11 items-center justify-center rounded-full bg-sky-600 px-5 text-sm font-semibold text-white transition hover:bg-sky-500 disabled:cursor-not-allowed disabled:bg-slate-300"
      disabled={props.disabled}
      type="submit"
    >
      {props.disabled ? "Saving..." : "Save settings"}
    </button>
  );
}

export function SiteSettingsForm(props: { settings: SiteSettingsResponse }) {
  const initialState: UpdateSiteSettingsActionState = {
    status: "idle",
    message: null,
    fields: props.settings,
  };

  const [state, formAction, isPending] = useActionState(
    updateSiteSettingsAction,
    initialState,
  );

  const messageClassName =
    state.status === "error"
      ? "text-rose-700"
      : state.status === "success"
        ? "text-emerald-700"
        : "text-slate-500";

  return (
    <form
      action={formAction}
      className="space-y-4"
      key={`${state.fields.title}-${state.fields.subtitle}-${state.fields.motd}-${state.status}`}
    >
      <label className="block space-y-2">
        <span className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
          Title
        </span>
        <input
          className="min-h-12 w-full rounded-2xl border border-slate-900/10 bg-slate-50 px-4 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:bg-white"
          defaultValue={state.fields.title}
          name="title"
          type="text"
        />
      </label>

      <label className="block space-y-2">
        <span className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
          Subtitle
        </span>
        <textarea
          className="min-h-24 w-full rounded-2xl border border-slate-900/10 bg-slate-50 px-4 py-3 text-sm leading-6 text-slate-900 outline-none transition focus:border-sky-500 focus:bg-white"
          defaultValue={state.fields.subtitle}
          name="subtitle"
        />
      </label>

      <label className="block space-y-2">
        <span className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
          MOTD
        </span>
        <textarea
          className="min-h-28 w-full rounded-2xl border border-slate-900/10 bg-slate-50 px-4 py-3 text-sm leading-6 text-slate-900 outline-none transition focus:border-sky-500 focus:bg-white"
          defaultValue={state.fields.motd}
          name="motd"
        />
      </label>

      <div className="flex items-center justify-between gap-4">
        <p aria-live="polite" className={`text-sm ${messageClassName}`}>
          {isPending
            ? "Saving changes..."
            : (state.message ?? "Ready to update.")}
        </p>
        <SubmitButton disabled={isPending} />
      </div>
    </form>
  );
}
