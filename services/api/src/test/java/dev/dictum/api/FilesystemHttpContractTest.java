package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.support.FilesystemContentFixture;
import dev.dictum.api.support.SessionHttpClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "dictum.content.repository=filesystem",
      "dictum.auth.admin.username=admin",
      "dictum.auth.admin.password=change-me"
    })
class FilesystemHttpContractTest {

  private static final String CONTENT_TYPE_HEADER = "content-type";
  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final Path CONTENT_ROOT = createContentRoot();
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "change-me";

  static {
    FilesystemContentFixture.writeSeed(CONTENT_ROOT);
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("dictum.content.root", CONTENT_ROOT::toString);
  }

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private SessionHttpClient sessionHttpClient;

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    FilesystemContentFixture.writeSeed(CONTENT_ROOT);
    sessionHttpClient = new SessionHttpClient("http://localhost:" + port, objectMapper);
  }

  @Test
  void listPostsReadsSeededFilesystemContent() throws Exception {
    HttpResponse<String> response = authenticatedGet("/api/v1/posts");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(value -> assertThat(value).contains(MediaType.APPLICATION_JSON_VALUE));

    JsonNode posts = objectMapper.readTree(response.body());
    assertThat(posts).hasSize(2);
    assertThat(posts.get(0).get("slug").asText()).isEqualTo("dictum-begins");
  }

  @Test
  void updateSiteSettingsWorksAgainstFilesystemPersistence() throws Exception {
    HttpResponse<String> response =
        authenticatedPatch(
            "PATCH",
            "/api/v1/settings/site",
            MERGE_PATCH_JSON,
            """
            {
              "subtitle": "A modular markdown blog platform.",
              "motd": "Filesystem-backed control plane wiring is live."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getSubtitle()).isEqualTo("A modular markdown blog platform.");
    assertThat(settings.getMotd()).isEqualTo("Filesystem-backed control plane wiring is live.");
  }

  private HttpResponse<String> authenticatedGet(String path)
      throws IOException, InterruptedException {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);
    return sessionHttpClient.get(path);
  }

  private HttpResponse<String> authenticatedPatch(
      String method, String path, String contentType, String body)
      throws IOException, InterruptedException {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);
    return sessionHttpClient.request(method, path, contentType, body, true);
  }

  private static Path createContentRoot() {
    try {
      return Files.createTempDirectory("dictum-filesystem-http-contract");
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to create temporary filesystem content root for HTTP contract tests", exception);
    }
  }
}
