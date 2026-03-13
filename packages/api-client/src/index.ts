export {
  Configuration,
  type CreatePostRequest,
  type PostResponse,
  type PostSummary,
  PostsApi,
  SiteSettingsApi,
  type SiteSettingsResponse,
  type UpdatePostRequest,
  type UpdateSiteSettingsRequest,
} from "./generated";

import { Configuration, PostsApi, SiteSettingsApi } from "./generated";

export function createControlPlaneApis(input: {
  basePath: string;
  accessToken?: string | (() => Promise<string> | string);
}) {
  const configuration = new Configuration({
    basePath: input.basePath,
    accessToken: input.accessToken,
  });

  return {
    configuration,
    posts: new PostsApi(configuration),
    siteSettings: new SiteSettingsApi(configuration),
  };
}
