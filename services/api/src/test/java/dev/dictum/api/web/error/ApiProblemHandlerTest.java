package dev.dictum.api.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.auth.error.InvalidCredentialsException;
import dev.dictum.api.content.error.InvalidPostRequestException;
import dev.dictum.api.content.error.PostAlreadyExistsException;
import dev.dictum.api.content.error.PostAlreadyPublishedException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.generated.model.ProblemDetails;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class ApiProblemHandlerTest {

  private static final String BAD_REQUEST_TITLE = "Bad request";
  private static final String DICTUM_BEGINS_SLUG = "dictum-begins";
  private static final String POST_PATH = path("api", "v1", "posts", DICTUM_BEGINS_SLUG);
  private static final String RESOURCE_NOT_FOUND_TITLE = "Resource not found";

  private ApiProblemHandler apiProblemHandler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    apiProblemHandler = new ApiProblemHandler(new ApiProblemFactory());
    request = new MockHttpServletRequest("PATCH", POST_PATH);
  }

  @Test
  void handlePostNotFoundReturnsNotFoundProblemDetails() {
    var response =
        apiProblemHandler.handlePostNotFound(new PostNotFoundException("unknown-slug"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/post-not-found",
        RESOURCE_NOT_FOUND_TITLE,
        "post.not_found",
        Map.of("slug", "unknown-slug"),
        404,
        POST_PATH);
  }

  @Test
  void handlePostAlreadyExistsReturnsConflictProblemDetails() {
    var response =
        apiProblemHandler.handlePostAlreadyExists(
            new PostAlreadyExistsException(DICTUM_BEGINS_SLUG), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/post-already-exists",
        "Conflict",
        "post.already_exists",
        Map.of("slug", DICTUM_BEGINS_SLUG),
        409,
        POST_PATH);
  }

  @Test
  void handlePostAlreadyPublishedReturnsConflictProblemDetails() {
    var response =
        apiProblemHandler.handlePostAlreadyPublished(
            new PostAlreadyPublishedException(DICTUM_BEGINS_SLUG), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/post-already-published",
        "Conflict",
        "post.already_published",
        Map.of("slug", DICTUM_BEGINS_SLUG),
        409,
        POST_PATH);
  }

  @Test
  void handleInvalidPatchReturnsBadRequestProblemDetails() {
    var response =
        apiProblemHandler.handleInvalidPatch(
            new InvalidPatchRequestException("Field subtitle cannot be null"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/invalid-patch-request",
        BAD_REQUEST_TITLE,
        "patch.invalid",
        Map.of(),
        400,
        POST_PATH);
  }

  @Test
  void handleInvalidPostRequestReturnsBadRequestProblemDetails() {
    var response =
        apiProblemHandler.handleInvalidPostRequest(
            new InvalidPostRequestException("Tags must not contain null values"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/invalid-post-request",
        BAD_REQUEST_TITLE,
        "post.invalid",
        Map.of(),
        400,
        POST_PATH);
  }

  @Test
  void handleUnsupportedMediaTypeReturnsProblemDetails() {
    var response =
        apiProblemHandler.handleUnsupportedMediaType(
            new HttpMediaTypeNotSupportedException("application/json"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/unsupported-media-type",
        "Unsupported media type",
        "request.unsupported_media_type",
        Map.of(),
        415,
        POST_PATH);
  }

  @Test
  void handleBadRequestReturnsProblemDetails() {
    var response =
        apiProblemHandler.handleBadRequest(
            new HttpMessageNotReadableException("Malformed JSON", (HttpInputMessage) null),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/bad-request",
        BAD_REQUEST_TITLE,
        "request.invalid",
        Map.of(),
        400,
        POST_PATH);
  }

  @Test
  void handleRequestNotFoundReturnsNotFoundProblemDetailsForMissingRoute() {
    var response =
        apiProblemHandler.handleRequestNotFound(
            new NoHandlerFoundException("GET", "/api/v1/missing", null), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/request-not-found",
        RESOURCE_NOT_FOUND_TITLE,
        "request.not_found",
        Map.of(),
        404,
        POST_PATH);
  }

  @Test
  void handleRequestNotFoundReturnsNotFoundProblemDetailsForMissingStaticResource() {
    var response =
        apiProblemHandler.handleRequestNotFound(
            new NoResourceFoundException(
                HttpMethod.GET, "/api/v1/missing", "No static resource found"),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/request-not-found",
        RESOURCE_NOT_FOUND_TITLE,
        "request.not_found",
        Map.of(),
        404,
        POST_PATH);
  }

  @Test
  void handleMethodNotAllowedReturnsProblemDetails() {
    var response =
        apiProblemHandler.handleMethodNotAllowed(
            new HttpRequestMethodNotSupportedException("PUT"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/method-not-allowed",
        "Method not allowed",
        "request.method_not_allowed",
        Map.of("method", "PUT"),
        405,
        POST_PATH);
  }

  @Test
  void handleInvalidCredentialsReturnsUnauthorizedProblemDetails() {
    var response =
        apiProblemHandler.handleInvalidCredentials(new InvalidCredentialsException(), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertProblem(
        response.getBody(),
        "https://dictum.dev/problems/invalid-credentials",
        "Authentication failed",
        "auth.invalid_credentials",
        Map.of(),
        401,
        POST_PATH);
  }

  private void assertProblem(
      ProblemDetails problemDetails,
      String type,
      String title,
      String code,
      Map<String, Object> params,
      int status,
      String instance) {
    assertThat(problemDetails).isNotNull();
    assertThat(problemDetails.getType()).isEqualTo(type);
    assertThat(problemDetails.getTitle()).isEqualTo(title);
    assertThat(problemDetails.getCode()).isEqualTo(code);
    assertThat(problemDetails.getParams()).isEqualTo(params);
    assertThat(problemDetails.getStatus()).isEqualTo(status);
    assertThat(problemDetails.getInstance()).isEqualTo(instance);
  }

  private static String path(String... segments) {
    return "/" + String.join("/", segments);
  }
}
