package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.support.InMemoryHttpContractSupport;
import dev.dictum.api.support.SessionHttpClient.HttpMethod;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PostsHttpContractTest extends InMemoryHttpContractSupport {

  @Test
  void protectedEndpointsRejectUnauthenticatedRequests() throws Exception {
    HttpResponse<String> response = sessionHttpClient.get(POSTS_PATH);

    assertThat(response.statusCode()).isEqualTo(401);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("auth.unauthenticated");
  }

  @Test
  void unsafeProtectedEndpointsReturnUnauthenticatedProblemWhenNoSessionExists() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.patchWithoutCsrf(
            REMOTE_CONTROLS_LATER_PATH, MERGE_PATCH_JSON, "{\"title\":\"No session\"}");

    assertThat(response.statusCode()).isEqualTo(401);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("auth.unauthenticated");
  }

  @Test
  void listPostsReturnsPublishedSummaries() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.getAuthenticated(POSTS_PATH, ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(value -> assertThat(value).contains(MediaType.APPLICATION_JSON_VALUE));

    JsonNode posts = objectMapper.readTree(response.body());
    assertThat(posts).isNotNull();
    assertThat(posts.isArray()).isTrue();
    assertThat(posts).hasSize(2);
    assertThat(posts.get(0).get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(posts.get(0).get("status").asText()).isEqualTo("published");
    assertThat(posts.get(1).get("slug").asText()).isEqualTo(REMOTE_CONTROLS_LATER_SLUG);
  }

  @Test
  void getPostReturnsTheFullPostRepresentation() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.getAuthenticated(
            path("api", "v1", POSTS_SEGMENT, DICTUM_BEGINS_SLUG), ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getSlug()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(post.getHasStylesheet()).isTrue();
    assertThat(post.getBody()).contains("hybrid stack");
  }

  @Test
  void getPostReturnsProblemDetailsForUnknownSlug() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.getAuthenticated(
            path("api", "v1", POSTS_SEGMENT, UNKNOWN_SLUG), ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-not-found");
    assertThat(problem.get("title").asText()).isEqualTo("Resource not found");
    assertThat(problem.get("code").asText()).isEqualTo("post.not_found");
    assertThat(problem.get(PARAMS_FIELD).get("slug").asText()).isEqualTo(UNKNOWN_SLUG);
    assertThat(problem.get("status").asInt()).isEqualTo(404);
  }

  @Test
  void missingApiRouteReturnsProblemDetails() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.getAuthenticated("/api/v1/missing", ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/request-not-found");
    assertThat(problem.get("title").asText()).isEqualTo("Resource not found");
    assertThat(problem.get("code").asText()).isEqualTo("request.not_found");
    assertThat(problem.get(PARAMS_FIELD).isEmpty()).isTrue();
    assertThat(problem.get("status").asInt()).isEqualTo(404);
  }

  @Test
  void unsupportedMethodReturnsProblemDetails() throws Exception {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    HttpResponse<String> response =
        sessionHttpClient.request(
            HttpMethod.PUT,
            REMOTE_CONTROLS_LATER_PATH,
            MediaType.APPLICATION_JSON_VALUE,
            "{\"title\":\"Wrong method\"}",
            true);

    assertThat(response.statusCode()).isEqualTo(405);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/method-not-allowed");
    assertThat(problem.get("title").asText()).isEqualTo("Method not allowed");
    assertThat(problem.get("code").asText()).isEqualTo("request.method_not_allowed");
    assertThat(problem.get(PARAMS_FIELD).get("method").asText()).isEqualTo("PUT");
    assertThat(problem.get("status").asInt()).isEqualTo(405);
  }

  @Test
  void createPostReturnsCreatedResourceAndLocationHeader() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.POST,
            POSTS_PATH,
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
            """,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

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
        sessionHttpClient.requestAuthenticated(
            HttpMethod.POST,
            POSTS_PATH,
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
            """,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("request.invalid");
  }

  @Test
  void createPostReturnsConflictProblemDetailsWhenSlugAlreadyExists() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.POST,
            POSTS_PATH,
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
            """,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(409);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-already-exists");
    assertThat(problem.get("code").asText()).isEqualTo("post.already_exists");
    assertThat(problem.get(PARAMS_FIELD).get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
  }

  @Test
  void updatePostAppliesMergePatchThroughTheHttpSurface() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.PATCH,
            REMOTE_CONTROLS_LATER_PATH,
            MERGE_PATCH_JSON,
            """
            {
              "title": "Remote Controls, Sooner",
              "tags": ["admin", "api"]
            }
            """,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getTitle()).isEqualTo("Remote Controls, Sooner");
    assertThat(post.getExcerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(post.getTags()).containsExactly(ADMIN_TAG, API_TAG);
  }

  @Test
  void updatePostRejectsWrongMediaType() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.PATCH,
            REMOTE_CONTROLS_LATER_PATH,
            MediaType.APPLICATION_JSON_VALUE,
            "{\"title\":\"Wrong media type\"}",
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(415);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("request.unsupported_media_type");
  }

  @Test
  void updatePostAcceptsLargeMergePatchBodies() throws Exception {
    String largeBody = "a".repeat(1_100_000);
    String payload = "{\"body\":\"" + largeBody + "\"}";

    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.PATCH,
            REMOTE_CONTROLS_LATER_PATH,
            MERGE_PATCH_JSON,
            payload,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getBody()).startsWith("a");
    assertThat(post.getBody()).hasSize(1_100_000);
  }

  @Test
  void publishPostTransitionsTheDraftToPublished() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.POST,
            REMOTE_CONTROLS_LATER_PATH + "/publish",
            null,
            null,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getStatus().getValue()).isEqualTo("published");
    assertThat(post.getPublishedAt()).isNotNull();
  }

  @Test
  void publishPostReturnsConflictProblemDetailsWhenAlreadyPublished() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.requestAuthenticated(
            HttpMethod.POST,
            path("api", "v1", POSTS_SEGMENT, DICTUM_BEGINS_SLUG, "publish"),
            null,
            null,
            ADMIN_USERNAME,
            ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(409);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-already-published");
    assertThat(problem.get("code").asText()).isEqualTo("post.already_published");
    assertThat(problem.get(PARAMS_FIELD).get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
  }

  @Test
  void unsafeRequestsRequireCsrfToken() throws Exception {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    HttpResponse<String> response =
        sessionHttpClient.patchWithoutCsrf(
            REMOTE_CONTROLS_LATER_PATH, MERGE_PATCH_JSON, "{\"title\":\"No CSRF\"}");

    assertThat(response.statusCode()).isEqualTo(403);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("request.csrf_invalid");
  }
}
