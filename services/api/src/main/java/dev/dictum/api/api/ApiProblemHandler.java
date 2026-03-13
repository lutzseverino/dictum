package dev.dictum.api.api;

import dev.dictum.api.generated.model.ProblemDetails;
import dev.dictum.api.posts.PostConflictException;
import dev.dictum.api.posts.PostNotFoundException;
import dev.dictum.api.settings.InvalidPatchRequestException;
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
    return problem(
        HttpStatus.BAD_REQUEST,
        "https://dictum.dev/problems/bad-request",
        "Bad request",
        exception.getMessage(),
        request);
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
    return problem(
        HttpStatus.BAD_REQUEST,
        "https://dictum.dev/problems/bad-request",
        "Bad request",
        exception.getMessage(),
        request);
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
}
