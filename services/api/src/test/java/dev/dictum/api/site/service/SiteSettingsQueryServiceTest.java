package dev.dictum.api.site.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.mapper.SiteSettingsApiMapperImpl;
import dev.dictum.api.site.store.InMemorySiteSettingsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SiteSettingsQueryServiceTest {

  private SiteSettingsQueryService siteSettingsQueryService;

  @BeforeEach
  void setUp() {
    siteSettingsQueryService =
        new SiteSettingsQueryService(
            new InMemorySiteSettingsStore(), new SiteSettingsApiMapperImpl());
  }

  @Test
  void getResponseReturnsTheSeededSiteSettings() {
    SiteSettingsResponse response = siteSettingsQueryService.getResponse();

    assertThat(response.getTitle()).isEqualTo("Dictum");
    assertThat(response.getSubtitle()).isEqualTo("A remotely steerable markdown blog kit.");
    assertThat(response.getMotd())
        .isEqualTo("Foundation mode is live: boundaries first, resources next.");
  }
}
