package dev.dictum.api.settings;

import dev.dictum.api.api.MergePatchBodyAccessor;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import org.springframework.stereotype.Service;

@Service
class SiteSettingsCommandService {

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
    requireNonNullWhenPresent("title", patchFields.title(), request.getTitle());
    requireNonNullWhenPresent("subtitle", patchFields.subtitle(), request.getSubtitle());
    requireNonNullWhenPresent("motd", patchFields.motd(), request.getMotd());
  }

  private void requireNonNullWhenPresent(String fieldName, boolean fieldPresent, Object value) {
    if (fieldPresent && value == null) {
      throw new InvalidPatchRequestException("Field " + fieldName + " cannot be null");
    }
  }

  private record SiteSettingsPatchFields(boolean title, boolean subtitle, boolean motd) {}
}
