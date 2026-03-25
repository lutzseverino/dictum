package dev.dictum.api.site.service;

import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import dev.dictum.api.site.store.SiteSettingsStore;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsCommandService {

  private final SiteSettingsStore siteSettingsStore;

  SiteSettingsCommandService(SiteSettingsStore siteSettingsStore) {
    this.siteSettingsStore = siteSettingsStore;
  }

  public SiteSettingsState update(SiteSettingsPatch patch) {
    SiteSettingsState current = siteSettingsStore.get();
    patch.validate();
    return siteSettingsStore.save(patch.applyTo(current));
  }
}
