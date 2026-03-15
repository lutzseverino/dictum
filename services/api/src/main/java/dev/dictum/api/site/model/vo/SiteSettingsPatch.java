package dev.dictum.api.site.model.vo;

import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.PatchValue;

public record SiteSettingsPatch(
    PatchValue<String> title, PatchValue<String> subtitle, PatchValue<String> motd) {

  public static SiteSettingsPatch from(
      UpdateSiteSettingsRequest request, MergePatchDocument document) {
    return new SiteSettingsPatch(
        document.field("title", request.getTitle()),
        document.field("subtitle", request.getSubtitle()),
        document.field("motd", request.getMotd()));
  }
}
