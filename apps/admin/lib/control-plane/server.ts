import "server-only";

import {
  createControlPlaneApis,
  FetchError,
  type PostSummary,
  type ProblemDetails,
  ResponseError,
  type SiteSettingsResponse,
  type UpdateSiteSettingsRequest,
} from "@dictum/api-client";

const DEFAULT_CONTROL_PLANE_URL = "http://localhost:8080";

export type ControlPlaneProblem = {
  title: string;
  detail: string;
  status?: number;
};

export type AdminDashboardData =
  | {
      posts: PostSummary[];
      siteSettings: SiteSettingsResponse;
      problem: null;
    }
  | {
      posts: PostSummary[];
      siteSettings: null;
      problem: ControlPlaneProblem;
    };

function createServerControlPlane() {
  return createControlPlaneApis({
    basePath: resolveControlPlaneUrl(),
  });
}

export async function getAdminDashboardData(): Promise<AdminDashboardData> {
  try {
    const controlPlane = createServerControlPlane();
    const [posts, siteSettings] = await Promise.all([
      controlPlane.posts.listPosts(),
      controlPlane.siteSettings.getSiteSettings(),
    ]);

    return {
      posts,
      siteSettings,
      problem: null,
    };
  } catch (error) {
    return {
      posts: [],
      siteSettings: null,
      problem: await readProblem(error),
    };
  }
}

export async function getSiteSettings(): Promise<SiteSettingsResponse> {
  const controlPlane = createServerControlPlane();
  return controlPlane.siteSettings.getSiteSettings();
}

export async function updateSiteSettings(
  request: UpdateSiteSettingsRequest,
): Promise<SiteSettingsResponse> {
  const controlPlane = createServerControlPlane();
  return controlPlane.siteSettings.updateSiteSettings({
    updateSiteSettingsRequest: request,
  });
}

function resolveControlPlaneUrl() {
  return (
    process.env.CONTROL_PLANE_URL ??
    process.env.NEXT_PUBLIC_CONTROL_PLANE_URL ??
    DEFAULT_CONTROL_PLANE_URL
  );
}

async function readProblem(error: unknown): Promise<ControlPlaneProblem> {
  if (error instanceof ResponseError) {
    const body = (await error.response
      .clone()
      .json()
      .catch(() => null)) as ProblemDetails | null;

    return {
      title: body?.title ?? "Control plane request failed",
      detail:
        body?.detail ??
        "The control plane responded with an error and the admin shell could not load live data.",
      status: body?.status,
    };
  }

  if (error instanceof FetchError) {
    return {
      title: "Control plane unavailable",
      detail:
        "The admin shell could not reach the Spring control plane. Start the API or point CONTROL_PLANE_URL at a reachable instance.",
    };
  }

  if (error instanceof Error) {
    return {
      title: "Unexpected admin error",
      detail: error.message,
    };
  }

  return {
    title: "Unexpected admin error",
    detail: "The admin shell could not load the control plane data.",
  };
}
