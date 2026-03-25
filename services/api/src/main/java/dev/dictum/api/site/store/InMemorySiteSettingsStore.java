package dev.dictum.api.site.store;

import dev.dictum.api.site.model.vo.SiteSettingsState;

public class InMemorySiteSettingsStore implements SiteSettingsStore {

  private SiteSettingsState siteSettings =
      new SiteSettingsState(
          "Dictum",
          "A remotely steerable markdown blog kit.",
          "Foundation mode is live: boundaries first, resources next.");

  @Override
  public synchronized SiteSettingsState get() {
    return siteSettings;
  }

  @Override
  public synchronized SiteSettingsState save(SiteSettingsState updated) {
    siteSettings = updated;
    return siteSettings;
  }
}
