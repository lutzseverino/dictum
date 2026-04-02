package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.SessionResponse;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.support.SessionHttpClient;
import java.io.IOException;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "dictum.content.repository=in-memory",
      "dictum.auth.admin.username=admin",
      "dictum.auth.admin.password=change-me"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HttpContractTest {

  private static final String CONTENT_TYPE_HEADER = "content-type";
  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final String POSTS_SEGMENT = "posts";
  private static final String DICTUM_BEGINS_SLUG = "dictum-begins";
  private static final String SESSION_PATH = path("api", "v1", "session");
  private static final String POSTS_PATH = path("api", "v1", POSTS_SEGMENT);
  private static final String REMOTE_CONTROLS_LATER_PATH =
      path("api", "v1", POSTS_SEGMENT, "remote-controls-later");
  private static final String SITE_SETTINGS_PATH = path("api", "v1", "settings", "site");
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "change-me";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private SessionHttpClient sessionHttpClient;

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    sessionHttpClient = new SessionHttpClient(baseUrl(), objectMapper);
  }

  @Test
  void getSessionReturnsCurrentAuthenticatedSession() throws Exception {
    HttpResponse<String> loginResponse =
        sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(loginResponse.statusCode()).isEqualTo(200);

    HttpResponse<String> response = sessionHttpClient.get(SESSION_PATH);

    assertThat(response.statusCode()).isEqualTo(200);

    SessionResponse session = objectMapper.readValue(response.body(), SessionResponse.class);
    assertThat(session.getUsername()).isEqualTo(ADMIN_USERNAME);
    assertThat(session.getCsrfToken()).isNotBlank();
  }

  @Test
  void createSessionRejectsInvalidCredentials() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.createSession(ADMIN_USERNAME, "wrong-password");

    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("auth.invalid_credentials");
  }

  @Test
  void protectedEndpointsRejectUnauthenticatedRequests() throws Exception {
    HttpResponse<String> response = sessionHttpClient.get(POSTS_PATH);

    assertThat(response.statusCode()).isEqualTo(401);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("auth.unauthenticated");
  }

  @Test
  void listPostsReturnsPublishedSummaries() throws Exception {
    HttpResponse<String> response = authenticatedGet(POSTS_PATH);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(value -> assertThat(value).contains(MediaType.APPLICATION_JSON_VALUE));

    JsonNode posts = objectMapper.readTree(response.body());
    assertThat(posts).isNotNull();
    assertThat(posts.isArray()).isTrue();
    assertThat(posts).hasSize(2);
    assertThat(posts.get(0).get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(posts.get(0).get("status").asText()).isEqualTo("published");
    assertThat(posts.get(1).get("slug").asText()).isEqualTo("remote-controls-later");
  }

  @Test
  void getPostReturnsTheFullPostRepresentation() throws Exception {
    HttpResponse<String> response =
        authenticatedGet(path("api", "v1", POSTS_SEGMENT, DICTUM_BEGINS_SLUG));

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getSlug()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(post.getHasStylesheet()).isTrue();
    assertThat(post.getBody()).contains("hybrid stack");
  }

  @Test
  void getPostReturnsProblemDetailsForUnknownSlug() throws Exception {
    HttpResponse<String> response =
        authenticatedGet(path("api", "v1", POSTS_SEGMENT, "unknown-slug"));

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-not-found");
    assertThat(problem.get("title").asText()).isEqualTo("Resource not found");
    assertThat(problem.get("code").asText()).isEqualTo("post.not_found");
    assertThat(problem.get("params").get("slug").asText()).isEqualTo("unknown-slug");
    assertThat(problem.get("status").asInt()).isEqualTo(404);
  }

  @Test
  void createPostReturnsCreatedResourceAndLocationHeader() throws Exception {
    HttpResponse<String> response =
        authenticatedRequest(
            "POST",
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
        authenticatedRequest(
            "POST",
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
            """);

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
        authenticatedRequest(
            "POST",
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
            """);

    assertThat(response.statusCode()).isEqualTo(409);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-already-exists");
    assertThat(problem.get("code").asText()).isEqualTo("post.already_exists");
    assertThat(problem.get("params").get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
  }

  @Test
  void updatePostAppliesMergePatchThroughTheHttpSurface() throws Exception {
    HttpResponse<String> response =
        authenticatedRequest(
            "PATCH",
            REMOTE_CONTROLS_LATER_PATH,
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
        authenticatedRequest(
            "PATCH",
            REMOTE_CONTROLS_LATER_PATH,
            MediaType.APPLICATION_JSON_VALUE,
            "{\"title\":\"Wrong media type\"}");

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
        authenticatedRequest("PATCH", REMOTE_CONTROLS_LATER_PATH, MERGE_PATCH_JSON, payload);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getBody()).startsWith("a");
    assertThat(post.getBody()).hasSize(1_100_000);
  }

  @Test
  void publishPostTransitionsTheDraftToPublished() throws Exception {
    HttpResponse<String> response =
        authenticatedRequest("POST", REMOTE_CONTROLS_LATER_PATH + "/publish", null, null);

    assertThat(response.statusCode()).isEqualTo(200);

    PostResponse post = objectMapper.readValue(response.body(), PostResponse.class);
    assertThat(post.getStatus().getValue()).isEqualTo("published");
    assertThat(post.getPublishedAt()).isNotNull();
  }

  @Test
  void publishPostReturnsConflictProblemDetailsWhenAlreadyPublished() throws Exception {
    HttpResponse<String> response =
        authenticatedRequest(
            "POST", path("api", "v1", POSTS_SEGMENT, DICTUM_BEGINS_SLUG, "publish"), null, null);

    assertThat(response.statusCode()).isEqualTo(409);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("type").asText())
        .isEqualTo("https://dictum.dev/problems/post-already-published");
    assertThat(problem.get("code").asText()).isEqualTo("post.already_published");
    assertThat(problem.get("params").get("slug").asText()).isEqualTo(DICTUM_BEGINS_SLUG);
  }

  @Test
  void getSiteSettingsReturnsTheCurrentSiteValues() throws Exception {
    HttpResponse<String> response = authenticatedGet(SITE_SETTINGS_PATH);

    assertThat(response.statusCode()).isEqualTo(200);

    SiteSettingsResponse settings =
        objectMapper.readValue(response.body(), SiteSettingsResponse.class);
    assertThat(settings.getTitle()).isEqualTo("Dictum");
    assertThat(settings.getSubtitle()).isEqualTo("A remotely steerable markdown blog kit.");
  }

  @Test
  void updateSiteSettingsReturnsTheUpdatedRepresentation() throws Exception {
    HttpResponse<String> response =
        authenticatedRequest(
            "PATCH",
            SITE_SETTINGS_PATH,
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

  @Test
  void unsafeRequestsRequireCsrfToken() throws Exception {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    HttpResponse<String> response =
        sessionHttpClient.request(
            "PATCH",
            REMOTE_CONTROLS_LATER_PATH,
            MERGE_PATCH_JSON,
            "{\"title\":\"No CSRF\"}",
            false);

    assertThat(response.statusCode()).isEqualTo(403);

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("request.csrf_invalid");
  }

  @Test
  void deleteSessionInvalidatesTheCurrentSession() throws Exception {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    HttpResponse<String> response = sessionHttpClient.delete(SESSION_PATH);

    assertThat(response.statusCode()).isEqualTo(204);

    HttpResponse<String> sessionResponse = sessionHttpClient.get(SESSION_PATH);
    assertThat(sessionResponse.statusCode()).isEqualTo(401);
  }

  private HttpResponse<String> authenticatedGet(String path)
      throws IOException, InterruptedException {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);
    return sessionHttpClient.get(path);
  }

  private HttpResponse<String> authenticatedRequest(
      String method, String path, String contentType, String body)
      throws IOException, InterruptedException {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);
    return sessionHttpClient.request(method, path, contentType, body, true);
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private static String path(String... segments) {
    return "/" + String.join("/", segments);
  }
}
