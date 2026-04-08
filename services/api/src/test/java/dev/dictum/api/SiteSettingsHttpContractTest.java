package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.support.InMemoryHttpContractSupport;
import dev.dictum.api.support.SessionHttpClient.HttpMethod;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class SiteSettingsHttpContractTest extends InMemoryHttpContractSupport {

  @Test
  void getSiteSettingsReturnsTheCurrentSiteValues() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.getAuthenticated(SITE_SETTINGS_PATH, ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getTitle()).isEqualTo("Dictum");
    assertThat(settings.getSubtitle()).isEqualTo("A remotely steerable markdown blog kit.");
  }

  @Test
  void updateSiteSettingsReturnsTheUpdatedRepresentation() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.PATCH,
            SITE_SETTINGS_PATH,
            MERGE_PATCH_JSON,
            """
            {
              "subtitle": "A modular markdown blog platform.",
              "motd": "API-first contract work is underway."
            }
            """,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getTitle()).isEqualTo("Dictum");
    assertThat(settings.getSubtitle()).isEqualTo("A modular markdown blog platform.");
    assertThat(settings.getMotd()).isEqualTo("API-first contract work is underway.");
  }
}
