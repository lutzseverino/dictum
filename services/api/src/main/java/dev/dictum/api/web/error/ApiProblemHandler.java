package dev.dictum.api.web.error;

import dev.dictum.api.content.error.InvalidPostRequestException;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.generated.model.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiProblemHandler {

  private static final String BAD_REQUEST_TYPE = "https://dictum.dev/problems/bad-request";
  private static final String BAD_REQUEST_TITLE = "Bad request";

  @ExceptionHandler(PostNotFoundException.class)
  public ResponseEntity<ProblemDetails> handlePostNotFound(
      PostNotFoundException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.NOT_FOUND,
        "https://dictum.dev/problems/not-found",
        "Resource not found",
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(PostConflictException.class)
  public ResponseEntity<ProblemDetails> handlePostConflict(
      PostConflictException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "https://dictum.dev/problems/conflict",
        "Conflict",
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(InvalidPatchRequestException.class)
  public ResponseEntity<ProblemDetails> handleInvalidPatch(
      InvalidPatchRequestException exception, HttpServletRequest request) {
    return badRequest(exception.getMessage(), request);
  }

  @ExceptionHandler(InvalidPostRequestException.class)
  public ResponseEntity<ProblemDetails> handleInvalidPostRequest(
      InvalidPostRequestException exception, HttpServletRequest request) {
    return badRequest(exception.getMessage(), request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ProblemDetails> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "https://dictum.dev/problems/unsupported-media-type",
        "Unsupported media type",
        exception.getMessage(),
        request);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ProblemDetails> handleBadRequest(
      Exception exception, HttpServletRequest request) {
    return badRequest(exception.getMessage(), request);
  }

  private ResponseEntity<ProblemDetails> problem(
      HttpStatus status, String type, String title, String detail, HttpServletRequest request) {
    ProblemDetails body =
        new ProblemDetails(title, status.value())
            .type(type)
            .detail(detail)
            .instance(URI.create(request.getRequestURI()).toString());

    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  private ResponseEntity<ProblemDetails> badRequest(String detail, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST_TYPE, BAD_REQUEST_TITLE, detail, request);
  }
}
