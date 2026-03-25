package dev.dictum.api.site.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.site.model.vo.SiteSettingsState;
import dev.dictum.api.site.store.InMemorySiteSettingsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SiteSettingsQueryServiceTest {

  private SiteSettingsQueryService siteSettingsQueryService;

  @BeforeEach
  void setUp() {
    siteSettingsQueryService = new SiteSettingsQueryService(new InMemorySiteSettingsStore());
  }

  @Test
  void getReturnsTheSeededSiteSettings() {
    SiteSettingsState response = siteSettingsQueryService.get();

    assertThat(response.title()).isEqualTo("Dictum");
    assertThat(response.subtitle()).isEqualTo("A remotely steerable markdown blog kit.");
    assertThat(response.motd())
        .isEqualTo("Foundation mode is live: boundaries first, resources next.");
  }
}
