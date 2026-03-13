package dev.dictum.api.settings;

import dev.dictum.api.api.MergePatchBodyAccessor;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsCommandService {

  private final InMemorySiteSettingsStore siteSettingsStore;
  private final SiteSettingsApiMapper siteSettingsApiMapper;
  private final MergePatchBodyAccessor mergePatchBodyAccessor;

  public SiteSettingsCommandService(
      InMemorySiteSettingsStore siteSettingsStore,
      SiteSettingsApiMapper siteSettingsApiMapper,
      MergePatchBodyAccessor mergePatchBodyAccessor) {
    this.siteSettingsStore = siteSettingsStore;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
    this.mergePatchBodyAccessor = mergePatchBodyAccessor;
  }

  public SiteSettingsResponse updateSiteSettings(UpdateSiteSettingsRequest request) {
    SiteSettingsState current = siteSettingsStore.get();
    mergePatchBodyAccessor.requireAnyField();

    if (mergePatchBodyAccessor.containsField("title") && request.getTitle() == null) {
      throw new InvalidPatchRequestException("Field title cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("subtitle") && request.getSubtitle() == null) {
      throw new InvalidPatchRequestException("Field subtitle cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("motd") && request.getMotd() == null) {
      throw new InvalidPatchRequestException("Field motd cannot be null");
    }

    SiteSettingsState updated =
        new SiteSettingsState(
            mergePatchBodyAccessor.containsField("title") ? request.getTitle() : current.title(),
            mergePatchBodyAccessor.containsField("subtitle")
                ? request.getSubtitle()
                : current.subtitle(),
            mergePatchBodyAccessor.containsField("motd") ? request.getMotd() : current.motd());

    return siteSettingsApiMapper.toResponse(siteSettingsStore.save(updated));
  }
}
