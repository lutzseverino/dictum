package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.support.FilesystemContentFixture;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class AuthConfigurationTest {

  @SuppressWarnings("java:S2068")
  private static final String ADMIN_USERNAME_PROPERTY = "dictum.auth.admin.username=admin";

  @SuppressWarnings("java:S2068")
  private static final String ADMIN_PASSWORD_PROPERTY = "dictum.auth.admin.password=change-me";

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner().withUserConfiguration(DictumApiApplication.class);

  @Test
  void authConfigurationRequiresConfiguredAdminCredentials() {
    contextRunner
        .withPropertyValues("dictum.content.repository=in-memory")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(rootCauseOf(context.getStartupFailure()))
                  .hasMessageContaining("dictum.auth.admin.username");
            });
  }

  @Test
  void authConfigurationStartsWithConfiguredAdminCredentials() {
    Path contentRoot = createContentRoot();
    FilesystemContentFixture.writeSeed(contentRoot);

    contextRunner
        .withPropertyValues(
            ADMIN_USERNAME_PROPERTY,
            ADMIN_PASSWORD_PROPERTY,
            "dictum.content.repository=filesystem",
            "dictum.content.root=" + contentRoot)
        .run(context -> assertThat(context).hasNotFailed());
  }

  private static Path createContentRoot() {
    try {
      return Files.createTempDirectory("dictum-auth-config");
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to create temporary content root for auth configuration tests", exception);
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
