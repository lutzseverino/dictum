package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HttpContractTest {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @LocalServerPort private int port;

  @Test
  void listPostsReturnsPublishedSummaries() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(value -> assertThat(value).contains(MediaType.APPLICATION_JSON_VALUE));

    JsonNode posts = objectMapper.readTree(response.body());
    assertThat(posts).isNotNull();
    assertThat(posts.isArray()).isTrue();
    assertThat(posts).hasSize(2);
    assertThat(posts.get(0).get("slug").asText()).isEqualTo("dictum-begins");
    assertThat(posts.get(0).get("status").asText()).isEqualTo("published");
    assertThat(posts.get(1).get("slug").asText()).isEqualTo("remote-controls-later");
  }

  @Test
  void getPostReturnsTheFullPostRepresentation() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts/dictum-begins");

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getSlug()).isEqualTo("dictum-begins");
    assertThat(post.getContentPath()).isEqualTo("posts/dictum-begins/index.md");
    assertThat(post.getHasStylesheet()).isTrue();
    assertThat(post.getBody()).contains("hybrid stack");
  }

  @Test
  void getPostReturnsProblemDetailsForUnknownSlug() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts/unknown-slug");

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("title").asText()).isEqualTo("Resource not found");
    assertThat(problem.get("status").asInt()).isEqualTo(404);
  }

  @Test
  void createPostReturnsCreatedResourceAndLocationHeader() throws Exception {
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/posts",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {
              "title": "Notes on Remote Editing",
              "slug": "notes-on-remote-editing",
              "excerpt": "First thoughts on a phone-first publishing workflow.",
              "template": "note",
              "tags": ["product", "mobile"],
              "body": "Remote editing should stay lightweight."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.headers().firstValue("location"))
        .hasValue("/api/v1/posts/notes-on-remote-editing");

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getSlug()).isEqualTo("notes-on-remote-editing");
    assertThat(post.getStatus().getValue()).isEqualTo("draft");
  }

  @Test
  void createPostRejectsUnknownRequestFields() throws Exception {
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/posts",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {
              "title": "Unknown Field",
              "slug": "unknown-field",
              "excerpt": "Unknown properties should not be ignored.",
              "template": "essay",
              "tags": ["architecture"],
              "body": "Unknown field body.",
              "unexpected": "value"
            }
            """);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void updatePostAppliesMergePatchThroughTheHttpSurface() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/posts/remote-controls-later",
            MERGE_PATCH_JSON,
            """
            {
              "title": "Remote Controls, Sooner",
              "tags": ["admin", "api"]
            }
            """);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getTitle()).isEqualTo("Remote Controls, Sooner");
    assertThat(post.getExcerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(post.getTags()).containsExactly("admin", "api");
  }

  @Test
  void updatePostRejectsWrongMediaType() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/posts/remote-controls-later",
            MediaType.APPLICATION_JSON_VALUE,
            "{\"title\":\"Wrong media type\"}");

    assertThat(response.statusCode()).isEqualTo(415);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void updatePostAcceptsLargeMergePatchBodies() throws Exception {
    String largeBody = "a".repeat(1_100_000);
    String payload = "{\"body\":\"" + largeBody + "\"}";

    HttpResponse<String> response =
        request("PATCH", "/api/v1/posts/remote-controls-later", MERGE_PATCH_JSON, payload);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getBody()).startsWith("a");
    assertThat(post.getBody()).hasSize(1_100_000);
  }

  @Test
  void publishPostTransitionsTheDraftToPublished() throws Exception {
    HttpResponse<String> response =
        request("POST", "/api/v1/posts/remote-controls-later/publish", null, null);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getStatus().getValue()).isEqualTo("published");
    assertThat(post.getPublishedAt()).isNotNull();
  }

  @Test
  void getSiteSettingsReturnsTheCurrentSiteValues() throws Exception {
    HttpResponse<String> response = get("/api/v1/settings/site");

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getTitle()).isEqualTo("Dictum");
    assertThat(settings.getSubtitle()).isEqualTo("A remotely steerable markdown blog kit.");
  }

  @Test
  void updateSiteSettingsReturnsTheUpdatedRepresentation() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/settings/site",
            MERGE_PATCH_JSON,
            """
            {
              "subtitle": "A modular markdown blog platform.",
              "motd": "API-first contract work is underway."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getTitle()).isEqualTo("Dictum");
    assertThat(settings.getSubtitle()).isEqualTo("A modular markdown blog platform.");
    assertThat(settings.getMotd()).isEqualTo("API-first contract work is underway.");
  }

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    return request("GET", path, null, null);
  }

  private HttpResponse<String> request(String method, String path, String contentType, String body)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl() + path));

    if (contentType != null) {
      builder.header("Content-Type", contentType);
    }

    HttpRequest request =
        switch (method) {
          case "POST" ->
              builder
                  .POST(
                      body == null
                          ? HttpRequest.BodyPublishers.noBody()
                          : HttpRequest.BodyPublishers.ofString(body))
                  .build();
          case "PATCH" ->
              builder
                  .method(
                      "PATCH",
                      body == null
                          ? HttpRequest.BodyPublishers.noBody()
                          : HttpRequest.BodyPublishers.ofString(body))
                  .build();
          default -> builder.GET().build();
        };

    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }
}
