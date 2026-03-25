package dev.dictum.api.site.store;

import dev.dictum.api.site.model.vo.SiteSettingsState;

public interface SiteSettingsStore {

  SiteSettingsState get();

  SiteSettingsState save(SiteSettingsState updated);
}
