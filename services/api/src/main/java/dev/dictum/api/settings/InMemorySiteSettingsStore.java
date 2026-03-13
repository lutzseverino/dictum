package dev.dictum.api.settings;

import org.springframework.stereotype.Component;

@Component
class InMemorySiteSettingsStore {

  private SiteSettingsState siteSettings =
      new SiteSettingsState(
          "Dictum",
          "A remotely steerable markdown blog kit.",
          "Foundation mode is live: boundaries first, resources next.");

  synchronized SiteSettingsState get() {
    return siteSettings;
  }

  synchronized SiteSettingsState save(SiteSettingsState updated) {
    siteSettings = updated;
    return siteSettings;
  }
}
