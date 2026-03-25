package dev.dictum.api.site.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import dev.dictum.api.site.store.InMemorySiteSettingsStore;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchDocument;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SiteSettingsCommandServiceTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private SiteSettingsCommandService siteSettingsCommandService;

  @BeforeEach
  void setUp() {
    siteSettingsCommandService = new SiteSettingsCommandService(new InMemorySiteSettingsStore());
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    UpdateSiteSettingsRequest request =
        new UpdateSiteSettingsRequest()
            .subtitle("A modular markdown blog platform.")
            .motd("API-first contract work is underway.");
    MergePatchDocument document =
        patchDocument(
            """
            {
              "subtitle": "A modular markdown blog platform.",
              "motd": "API-first contract work is underway."
            }
            """);

    SiteSettingsState updated =
        siteSettingsCommandService.update(
            new SiteSettingsPatch(
                document.field("title", request.getTitle()),
                document.field("subtitle", request.getSubtitle()),
                document.field("motd", request.getMotd())));

    assertThat(updated.title()).isEqualTo("Dictum");
    assertThat(updated.subtitle()).isEqualTo("A modular markdown blog platform.");
    assertThat(updated.motd()).isEqualTo("API-first contract work is underway.");
  }

  @Test
  void updateRejectsExplicitNullForPresentFields() {
    UpdateSiteSettingsRequest request = new UpdateSiteSettingsRequest().subtitle(null);
    MergePatchDocument document = patchDocument("{\"subtitle\":null}");

    assertThatThrownBy(
            () ->
                siteSettingsCommandService.update(
                    new SiteSettingsPatch(
                        document.field("title", request.getTitle()),
                        document.field("subtitle", request.getSubtitle()),
                        document.field("motd", request.getMotd()))))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field subtitle cannot be null");
  }

  private MergePatchDocument patchDocument(String json) {
    try {
      return new MergePatchDocument(OBJECT_MAPPER.readTree(json));
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to create merge patch document for tests", exception);
    }
  }
}
