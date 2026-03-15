package dev.dictum.api.site.repository;

import dev.dictum.api.site.model.vo.SiteSettingsState;

public interface SiteSettingsRepository {

  SiteSettingsState get();

  SiteSettingsState save(SiteSettingsState updated);
}
