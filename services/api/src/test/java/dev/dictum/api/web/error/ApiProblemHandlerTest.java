package dev.dictum.api.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import dev.dictum.api.content.error.InvalidPostRequestException;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.generated.model.ProblemDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;

class ApiProblemHandlerTest {

  private ApiProblemHandler apiProblemHandler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    apiProblemHandler = new ApiProblemHandler();
    request = new MockHttpServletRequest("PATCH", "/api/v1/posts/dictum-begins");
  }

  @Test
  void handlePostNotFoundReturnsNotFoundProblemDetails() {
    var response =
        apiProblemHandler.handlePostNotFound(
            new PostNotFoundException("No post exists for slug unknown-slug"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertProblem(response.getBody(), "Resource not found", 404, "/api/v1/posts/dictum-begins");
  }

  @Test
  void handlePostConflictReturnsConflictProblemDetails() {
    var response =
        apiProblemHandler.handlePostConflict(
            new PostConflictException("Post dictum-begins is already published"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertProblem(response.getBody(), "Conflict", 409, "/api/v1/posts/dictum-begins");
  }

  @Test
  void handleInvalidPatchReturnsBadRequestProblemDetails() {
    var response =
        apiProblemHandler.handleInvalidPatch(
            new InvalidPatchRequestException("Field subtitle cannot be null"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(response.getBody(), "Bad request", 400, "/api/v1/posts/dictum-begins");
  }

  @Test
  void handleInvalidPostRequestReturnsBadRequestProblemDetails() {
    var response =
        apiProblemHandler.handleInvalidPostRequest(
            new InvalidPostRequestException("Tags must not contain null values"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(response.getBody(), "Bad request", 400, "/api/v1/posts/dictum-begins");
  }

  @Test
  void handleUnsupportedMediaTypeReturnsProblemDetails() {
    var response =
        apiProblemHandler.handleUnsupportedMediaType(
            new HttpMediaTypeNotSupportedException("application/json"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    assertProblem(response.getBody(), "Unsupported media type", 415, "/api/v1/posts/dictum-begins");
  }

  @Test
  void handleBadRequestReturnsProblemDetails() {
    var response =
        apiProblemHandler.handleBadRequest(
            new HttpMessageNotReadableException("Malformed JSON", (HttpInputMessage) null),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertProblem(response.getBody(), "Bad request", 400, "/api/v1/posts/dictum-begins");
  }

  private void assertProblem(
      ProblemDetails problemDetails, String title, int status, String instance) {
    assertThat(problemDetails).isNotNull();
    assertThat(problemDetails.getTitle()).isEqualTo(title);
    assertThat(problemDetails.getStatus()).isEqualTo(status);
    assertThat(problemDetails.getInstance()).isEqualTo(instance);
  }
}
