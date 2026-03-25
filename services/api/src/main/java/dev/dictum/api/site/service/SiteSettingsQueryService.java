package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.mapper.SiteSettingsApiMapper;
import dev.dictum.api.site.store.SiteSettingsStore;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsQueryService {

  private final SiteSettingsStore siteSettingsStore;
  private final SiteSettingsApiMapper siteSettingsApiMapper;

  SiteSettingsQueryService(
      SiteSettingsStore siteSettingsStore, SiteSettingsApiMapper siteSettingsApiMapper) {
    this.siteSettingsStore = siteSettingsStore;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
  }

  public SiteSettingsResponse getResponse() {
    return siteSettingsApiMapper.toResponse(siteSettingsStore.get());
  }
}
