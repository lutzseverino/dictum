package dev.dictum.api.site.store;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.config.FilesystemContentRoot;
import dev.dictum.api.site.model.state.SiteSettingsState;
import dev.dictum.api.support.FilesystemContentFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemSiteSettingsStoreTest {

  @TempDir private Path contentRoot;

  private FilesystemSiteSettingsStore siteSettingsStore;

  @BeforeEach
  void setUp() {
    FilesystemContentFixture.writeSeed(contentRoot);
    siteSettingsStore = new FilesystemSiteSettingsStore(FilesystemContentRoot.from(contentRoot));
  }

  @Test
  void getReadsTheSeededSiteSettings() {
    SiteSettingsState settings = siteSettingsStore.get();

    assertThat(settings.title()).isEqualTo("Dictum");
    assertThat(settings.subtitle()).isEqualTo("A remotely steerable markdown blog kit.");
  }

  @Test
  void savePersistsUpdatedSettings() throws Exception {
    SiteSettingsState updated =
        new SiteSettingsState(
            "Dictum",
            "A modular markdown blog platform.",
            "Filesystem-backed control plane wiring is live.");

    SiteSettingsState saved = siteSettingsStore.save(updated);

    assertThat(saved).isEqualTo(updated);
    assertThat(Files.readString(contentRoot.resolve("settings/site.json")))
        .contains("\"subtitle\":\"A modular markdown blog platform.\"")
        .contains("\"motd\":\"Filesystem-backed control plane wiring is live.\"");
  }
}
