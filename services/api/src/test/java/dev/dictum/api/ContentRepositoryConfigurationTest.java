package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.content.repository.FilesystemPostRepository;
import dev.dictum.api.content.repository.InMemoryPostRepository;
import dev.dictum.api.site.repository.FilesystemSiteSettingsRepository;
import dev.dictum.api.site.repository.InMemorySiteSettingsRepository;
import dev.dictum.api.support.FilesystemContentFixture;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class ContentRepositoryConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner().withUserConfiguration(DictumApiApplication.class);

  @Test
  void filesystemModeRequiresContentRoot() {
    contextRunner
        .withPropertyValues("dictum.content.repository=filesystem")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .hasRootCauseInstanceOf(IllegalStateException.class);
              assertThat(rootCauseOf(context.getStartupFailure()))
                  .hasMessageContaining("dictum.content.root");
            });
  }

  @Test
  void explicitInMemoryModeStartsWithoutContentRoot() {
    contextRunner
        .withPropertyValues("dictum.content.repository=in-memory")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(InMemoryPostRepository.class);
              assertThat(context).hasSingleBean(InMemorySiteSettingsRepository.class);
              assertThat(context).doesNotHaveBean(FilesystemPostRepository.class);
              assertThat(context).doesNotHaveBean(FilesystemSiteSettingsRepository.class);
            });
  }

  @Test
  void filesystemModeStartsWhenContentRootIsConfigured() {
    Path contentRoot = createContentRoot();
    FilesystemContentFixture.writeSeed(contentRoot);

    contextRunner
        .withPropertyValues(
            "dictum.content.repository=filesystem", "dictum.content.root=" + contentRoot)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(FilesystemPostRepository.class);
              assertThat(context).hasSingleBean(FilesystemSiteSettingsRepository.class);
              assertThat(context).doesNotHaveBean(InMemoryPostRepository.class);
              assertThat(context).doesNotHaveBean(InMemorySiteSettingsRepository.class);
            });
  }

  private static Path createContentRoot() {
    try {
      return Files.createTempDirectory("dictum-content-config");
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to create temporary content root for configuration tests", exception);
    }
  }

  private static Throwable rootCauseOf(Throwable throwable) {
    Throwable current = throwable;

    while (current.getCause() != null) {
      current = current.getCause();
    }

    return current;
  }
}
