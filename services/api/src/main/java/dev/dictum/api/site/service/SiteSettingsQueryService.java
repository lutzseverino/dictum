package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.mapper.SiteSettingsApiMapper;
import dev.dictum.api.site.repository.InMemorySiteSettingsStore;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsQueryService {

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
