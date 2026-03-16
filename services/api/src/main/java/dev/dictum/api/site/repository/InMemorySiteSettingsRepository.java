package dev.dictum.api.site.repository;

import dev.dictum.api.site.model.vo.SiteSettingsState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
    name = "dictum.content.repository",
    havingValue = "in-memory",
    matchIfMissing = true)
public class InMemorySiteSettingsRepository implements SiteSettingsRepository {

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
