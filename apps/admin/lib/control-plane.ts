import { createControlPlaneApis } from "@dictum/api-client";

const DEFAULT_CONTROL_PLANE_URL = "http://localhost:8080";

export const adminControlPlane = createControlPlaneApis({
  basePath:
    process.env.NEXT_PUBLIC_CONTROL_PLANE_URL ?? DEFAULT_CONTROL_PLANE_URL,
});
