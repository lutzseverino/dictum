package dev.dictum.api.site.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.mapper.SiteSettingsApiMapperImpl;
import dev.dictum.api.site.repository.InMemorySiteSettingsStore;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchBodyAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteSettingsCommandServiceTest {

  @Mock private MergePatchBodyAccessor mergePatchBodyAccessor;

  private SiteSettingsCommandService siteSettingsCommandService;

  @BeforeEach
  void setUp() {
    siteSettingsCommandService =
        new SiteSettingsCommandService(
            new InMemorySiteSettingsStore(),
            new SiteSettingsApiMapperImpl(),
            mergePatchBodyAccessor);
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    when(mergePatchBodyAccessor.containsField("title")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("subtitle")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("motd")).thenReturn(true);

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
    when(mergePatchBodyAccessor.containsField("title")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("subtitle")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("motd")).thenReturn(false);

    assertThatThrownBy(
            () -> siteSettingsCommandService.update(new UpdateSiteSettingsRequest().subtitle(null)))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field subtitle cannot be null");
  }
}
