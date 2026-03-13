package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

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
class DictumApiApplicationTests {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";

  @LocalServerPort private int port;

  @Test
  void listPostsReturnsGeneratedSummaryShape() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(value -> assertThat(value).contains("application/json"));
    assertThat(response.body()).contains("\"slug\":\"dictum-begins\"");
    assertThat(response.body()).contains("\"status\":\"published\"");
    assertThat(response.body()).contains("\"slug\":\"remote-controls-later\"");
  }

  @Test
  void getPostReturnsFullResponseShape() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts/dictum-begins");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"contentPath\":\"posts/dictum-begins/index.md\"");
    assertThat(response.body()).contains("\"hasStylesheet\":true");
    assertThat(response.body()).contains("\"body\":");
  }

  @Test
  void getPostReturnsProblemForUnknownSlug() throws Exception {
    HttpResponse<String> response = get("/api/v1/posts/unknown-slug");
    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    assertThat(response.body()).contains("\"title\":\"Resource not found\"");
    assertThat(response.body()).contains("\"status\":404");
  }

  @Test
  void createPostReturnsCreatedResourceAndLocation() throws Exception {
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
    assertThat(response.body()).contains("\"slug\":\"notes-on-remote-editing\"");
    assertThat(response.body()).contains("\"status\":\"draft\"");
  }

  @Test
  void createPostRejectsDuplicateSlug() throws Exception {
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/posts",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {
              "title": "Duplicate",
              "slug": "dictum-begins",
              "excerpt": "Already exists.",
              "template": "essay",
              "tags": ["architecture"],
              "body": "Duplicate body."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(409);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void createPostRejectsSlugOutsideThePublishedSlugFormat() throws Exception {
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/posts",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {
              "title": "Invalid Slug",
              "slug": "Invalid Slug",
              "excerpt": "Slug contains spaces and capitals.",
              "template": "essay",
              "tags": ["architecture"],
              "body": "Invalid slug body."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void createPostRejectsTagsContainingNullEntries() throws Exception {
    HttpResponse<String> response =
        request(
            "POST",
            "/api/v1/posts",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {
              "title": "Invalid Tags",
              "slug": "invalid-tags",
              "excerpt": "Tags contain a null item.",
              "template": "essay",
              "tags": ["architecture", null],
              "body": "Invalid tags body."
            }
            """);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void updatePostAppliesOnlyProvidedFields() throws Exception {
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
    assertThat(response.body()).contains("\"title\":\"Remote Controls, Sooner\"");
    assertThat(response.body())
        .contains(
            "\"excerpt\":\"The admin experience will later own publish and settings mutations.\"");
    assertThat(response.body()).contains("\"tags\":[\"admin\",\"api\"]");
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
  void updatePostAllowsRemovingStylesheetWithExplicitNull() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/posts/dictum-begins",
            MERGE_PATCH_JSON,
            """
            {
              "stylesheet": null
            }
            """);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"hasStylesheet\":false");
    assertThat(response.body()).contains("\"stylesheetPath\":null");
    assertThat(response.body()).contains("\"title\":\"Dictum Begins\"");
  }

  @Test
  void updatePostRejectsTagsContainingNullEntries() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/posts/remote-controls-later",
            MERGE_PATCH_JSON,
            """
            {
              "tags": ["admin", null]
            }
            """);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void publishPostTransitionsDraftToPublished() throws Exception {
    HttpResponse<String> response =
        request("POST", "/api/v1/posts/remote-controls-later/publish", null, null);
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"status\":\"published\"");
    assertThat(response.body()).contains("\"publishedAt\":\"");
  }

  @Test
  void publishPostRejectsAlreadyPublishedPost() throws Exception {
    HttpResponse<String> response =
        request("POST", "/api/v1/posts/dictum-begins/publish", null, null);
    assertThat(response.statusCode()).isEqualTo(409);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
  }

  @Test
  void siteSettingsEndpointsReturnAndUpdateSettings() throws Exception {
    HttpResponse<String> getResponse = get("/api/v1/settings/site");
    assertThat(getResponse.statusCode()).isEqualTo(200);
    assertThat(getResponse.body()).contains("\"title\":\"Dictum\"");

    HttpResponse<String> patchResponse =
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

    assertThat(patchResponse.statusCode()).isEqualTo(200);
    assertThat(patchResponse.body()).contains("\"title\":\"Dictum\"");
    assertThat(patchResponse.body()).contains("\"subtitle\":\"A modular markdown blog platform.\"");
    assertThat(patchResponse.body()).contains("\"motd\":\"API-first contract work is underway.\"");
  }

  @Test
  void updateSiteSettingsRejectsExplicitNullField() throws Exception {
    HttpResponse<String> response =
        request(
            "PATCH",
            "/api/v1/settings/site",
            MERGE_PATCH_JSON,
            """
            {
              "subtitle": null
            }
            """);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue("content-type"))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    assertThat(response.body()).contains("\"title\":\"Bad request\"");
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
