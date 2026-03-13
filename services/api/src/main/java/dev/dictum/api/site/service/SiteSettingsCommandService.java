package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.model.vo.SiteSettingsPatchFields;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import dev.dictum.api.site.repository.InMemorySiteSettingsStore;
import dev.dictum.api.web.patch.MergePatchBodyAccessor;
import dev.dictum.api.web.patch.MergePatchFieldRules;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsCommandService {

  private final InMemorySiteSettingsStore siteSettingsStore;
  private final SiteSettingsApiMapper siteSettingsApiMapper;
  private final MergePatchBodyAccessor mergePatchBodyAccessor;

  SiteSettingsCommandService(
      InMemorySiteSettingsStore siteSettingsStore,
      SiteSettingsApiMapper siteSettingsApiMapper,
      MergePatchBodyAccessor mergePatchBodyAccessor) {
    this.siteSettingsStore = siteSettingsStore;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
    this.mergePatchBodyAccessor = mergePatchBodyAccessor;
  }

  public SiteSettingsResponse update(UpdateSiteSettingsRequest request) {
    SiteSettingsState current = siteSettingsStore.get();
    SiteSettingsPatchFields patchFields = readPatchFields();
    validateUpdateRequest(request, patchFields);

    SiteSettingsState updated =
        new SiteSettingsState(
            patchFields.title() ? request.getTitle() : current.title(),
            patchFields.subtitle() ? request.getSubtitle() : current.subtitle(),
            patchFields.motd() ? request.getMotd() : current.motd());

    return siteSettingsApiMapper.toResponse(siteSettingsStore.save(updated));
  }

  private SiteSettingsPatchFields readPatchFields() {
    mergePatchBodyAccessor.requireAnyField();

    return new SiteSettingsPatchFields(
        mergePatchBodyAccessor.containsField("title"),
        mergePatchBodyAccessor.containsField("subtitle"),
        mergePatchBodyAccessor.containsField("motd"));
  }

  private void validateUpdateRequest(
      UpdateSiteSettingsRequest request, SiteSettingsPatchFields patchFields) {
    MergePatchFieldRules.requireNonNullWhenPresent(
        "title", patchFields.title(), request.getTitle());
    MergePatchFieldRules.requireNonNullWhenPresent(
        "subtitle", patchFields.subtitle(), request.getSubtitle());
    MergePatchFieldRules.requireNonNullWhenPresent("motd", patchFields.motd(), request.getMotd());
  }
}
