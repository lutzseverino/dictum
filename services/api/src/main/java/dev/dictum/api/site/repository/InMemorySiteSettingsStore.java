package dev.dictum.api.site.repository;

import dev.dictum.api.site.model.vo.SiteSettingsState;
import org.springframework.stereotype.Component;

@Component
public class InMemorySiteSettingsStore {

  private SiteSettingsState siteSettings =
      new SiteSettingsState(
          "Dictum",
          "A remotely steerable markdown blog kit.",
          "Foundation mode is live: boundaries first, resources next.");

  public synchronized SiteSettingsState get() {
    return siteSettings;
  }

  public synchronized SiteSettingsState save(SiteSettingsState updated) {
    siteSettings = updated;
    return siteSettings;
  }
}
