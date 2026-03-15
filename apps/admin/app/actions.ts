"use server";

import type {
  SiteSettingsResponse,
  UpdateSiteSettingsRequest,
} from "@dictum/api-client";
import { revalidatePath } from "next/cache";
import {
  getSiteSettings,
  updateSiteSettings,
} from "@/lib/control-plane/server";

export type UpdateSiteSettingsActionState = {
  status: "idle" | "success" | "error";
  message: string | null;
  fields: SiteSettingsResponse;
};

export async function updateSiteSettingsAction(
  _previousState: UpdateSiteSettingsActionState,
  formData: FormData,
): Promise<UpdateSiteSettingsActionState> {
  const submittedFields = readSubmittedFields(formData);

  try {
    const currentSettings = await getSiteSettings();
    const patch = buildSiteSettingsPatch(currentSettings, submittedFields);

    if (!patch) {
      return {
        status: "idle",
        message: "No changes to save.",
        fields: currentSettings,
      };
    }

    const updatedSettings = await updateSiteSettings(patch);
    revalidatePath("/");

    return {
      status: "success",
      message: "Site settings updated.",
      fields: updatedSettings,
    };
  } catch (error) {
    return {
      status: "error",
      message:
        error instanceof Error
          ? error.message
          : "The site settings could not be updated.",
      fields: submittedFields,
    };
  }
}

function readSubmittedFields(formData: FormData): SiteSettingsResponse {
  return {
    title: readTextField(formData, "title"),
    subtitle: readTextField(formData, "subtitle"),
    motd: readTextField(formData, "motd"),
  };
}

function buildSiteSettingsPatch(
  currentSettings: SiteSettingsResponse,
  submittedFields: SiteSettingsResponse,
): UpdateSiteSettingsRequest | null {
  const patch: UpdateSiteSettingsRequest = {};

  if (submittedFields.title !== currentSettings.title) {
    patch.title = submittedFields.title;
  }

  if (submittedFields.subtitle !== currentSettings.subtitle) {
    patch.subtitle = submittedFields.subtitle;
  }

  if (submittedFields.motd !== currentSettings.motd) {
    patch.motd = submittedFields.motd;
  }

  return Object.keys(patch).length > 0 ? patch : null;
}

function readTextField(formData: FormData, fieldName: string) {
  const value = formData.get(fieldName);
  return typeof value === "string" ? value : "";
}
