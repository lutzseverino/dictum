package dev.dictum.api.site.service;

import dev.dictum.api.site.model.state.SiteSettingsState;
import dev.dictum.api.site.store.SiteSettingsStore;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsQueryService {

  private final SiteSettingsStore siteSettingsStore;

  SiteSettingsQueryService(SiteSettingsStore siteSettingsStore) {
    this.siteSettingsStore = siteSettingsStore;
  }

  public SiteSettingsState get() {
    return siteSettingsStore.get();
  }
}
