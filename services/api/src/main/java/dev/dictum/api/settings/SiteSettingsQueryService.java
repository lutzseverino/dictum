package dev.dictum.api.settings;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import org.springframework.stereotype.Service;

@Service
class SiteSettingsQueryService {

  private final InMemorySiteSettingsStore siteSettingsStore;
  private final SiteSettingsApiMapper siteSettingsApiMapper;

  SiteSettingsQueryService(
      InMemorySiteSettingsStore siteSettingsStore, SiteSettingsApiMapper siteSettingsApiMapper) {
    this.siteSettingsStore = siteSettingsStore;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
  }

  public SiteSettingsResponse getResponse() {
    return siteSettingsApiMapper.toResponse(siteSettingsStore.get());
  }
}
