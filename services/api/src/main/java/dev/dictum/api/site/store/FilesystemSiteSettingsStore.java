package dev.dictum.api.site.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.config.FilesystemContentRoot;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemSiteSettingsStore implements SiteSettingsStore {
  private final Path settingsFile;
  private final ObjectMapper objectMapper;

  public FilesystemSiteSettingsStore(FilesystemContentRoot contentRoot) {
    this.settingsFile = contentRoot.siteSettingsFile();
    this.objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Override
  public SiteSettingsState get() {
    try {
      return objectMapper.readValue(Files.readString(settingsFile), SiteSettingsState.class);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to read site settings from content repository", exception);
    }
  }

  @Override
  public SiteSettingsState save(SiteSettingsState updated) {
    try {
      Files.writeString(
          settingsFile, objectMapper.writeValueAsString(updated), StandardCharsets.UTF_8);
      return get();
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to write site settings to content repository", exception);
    }
  }
}
