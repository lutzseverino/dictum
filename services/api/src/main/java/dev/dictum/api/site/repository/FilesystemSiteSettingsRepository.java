package dev.dictum.api.site.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "dictum.content.repository", havingValue = "filesystem")
public class FilesystemSiteSettingsRepository implements SiteSettingsRepository {

  private static final String SETTINGS_DIRECTORY = "settings";
  private static final String SITE_SETTINGS_FILENAME = "site.json";

  private final Path settingsFile;
  private final ObjectMapper objectMapper;

  public FilesystemSiteSettingsRepository(@Value("${dictum.content.root}") String configuredRoot) {
    Path contentRoot = validateRoot(configuredRoot);
    this.settingsFile = contentRoot.resolve(SETTINGS_DIRECTORY).resolve(SITE_SETTINGS_FILENAME);
    this.objectMapper = new ObjectMapper().findAndRegisterModules();
    ensureSettingsFileExists();
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

  private void ensureSettingsFileExists() {
    Path settingsDirectory = settingsFile.getParent();

    try {
      Files.createDirectories(settingsDirectory);
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to initialize settings directory", exception);
    }

    if (!Files.exists(settingsFile)) {
      throw new IllegalStateException("Content repository is missing settings/site.json");
    }
  }

  private static Path validateRoot(String configuredRoot) {
    if (configuredRoot == null || configuredRoot.isBlank()) {
      throw new IllegalStateException(
          "Property dictum.content.root must be configured when using the filesystem repository");
    }

    return Path.of(configuredRoot);
  }
}
