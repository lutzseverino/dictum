package dev.dictum.api.site.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.mapper.SiteSettingsApiMapperImpl;
import dev.dictum.api.site.repository.InMemorySiteSettingsRepository;
import dev.dictum.api.site.rule.SiteSettingsPatchRequiredValuesRule;
import dev.dictum.api.site.rule.SiteSettingsPatchValidator;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteSettingsCommandServiceTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mock private MergePatchDocumentAccessor mergePatchDocumentAccessor;

  private SiteSettingsCommandService siteSettingsCommandService;

  @BeforeEach
  void setUp() {
    siteSettingsCommandService =
        new SiteSettingsCommandService(
            new InMemorySiteSettingsRepository(),
            new SiteSettingsApiMapperImpl(),
            new SiteSettingsPatchValidator(List.of(new SiteSettingsPatchRequiredValuesRule())),
            mergePatchDocumentAccessor);
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    when(mergePatchDocumentAccessor.currentDocument())
        .thenReturn(
            patchDocument(
                """
                {
                  "subtitle": "A modular markdown blog platform.",
                  "motd": "API-first contract work is underway."
                }
                """));

    SiteSettingsResponse response =
        siteSettingsCommandService.update(
            new UpdateSiteSettingsRequest()
                .subtitle("A modular markdown blog platform.")
                .motd("API-first contract work is underway."));

    assertThat(response.getTitle()).isEqualTo("Dictum");
    assertThat(response.getSubtitle()).isEqualTo("A modular markdown blog platform.");
    assertThat(response.getMotd()).isEqualTo("API-first contract work is underway.");
  }

  @Test
  void updateRejectsExplicitNullForPresentFields() {
    when(mergePatchDocumentAccessor.currentDocument())
        .thenReturn(patchDocument("{\"subtitle\":null}"));

    assertThatThrownBy(
            () -> siteSettingsCommandService.update(new UpdateSiteSettingsRequest().subtitle(null)))
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
