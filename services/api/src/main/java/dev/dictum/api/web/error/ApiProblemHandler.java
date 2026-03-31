package dev.dictum.api.web.error;

import dev.dictum.api.content.error.InvalidPostRequestException;
import dev.dictum.api.content.error.PostAlreadyExistsException;
import dev.dictum.api.content.error.PostAlreadyPublishedException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.generated.model.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
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
    return problem(ApiProblemSpec.postNotFound(exception), exception.getMessage(), request);
  }

  @ExceptionHandler(PostAlreadyExistsException.class)
  public ResponseEntity<ProblemDetails> handlePostAlreadyExists(
      PostAlreadyExistsException exception, HttpServletRequest request) {
    return problem(ApiProblemSpec.postAlreadyExists(exception), exception.getMessage(), request);
  }

  @ExceptionHandler(PostAlreadyPublishedException.class)
  public ResponseEntity<ProblemDetails> handlePostAlreadyPublished(
      PostAlreadyPublishedException exception, HttpServletRequest request) {
    return problem(ApiProblemSpec.postAlreadyPublished(exception), exception.getMessage(), request);
  }

  @ExceptionHandler(InvalidPatchRequestException.class)
  public ResponseEntity<ProblemDetails> handleInvalidPatch(
      InvalidPatchRequestException exception, HttpServletRequest request) {
    return problem(ApiProblemSpec.invalidPatchRequest(), exception.getMessage(), request);
  }

  @ExceptionHandler(InvalidPostRequestException.class)
  public ResponseEntity<ProblemDetails> handleInvalidPostRequest(
      InvalidPostRequestException exception, HttpServletRequest request) {
    return problem(ApiProblemSpec.invalidPostRequest(), exception.getMessage(), request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ProblemDetails> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
    return problem(
        ApiProblemSpec.unsupportedMediaType(exception.getContentType()),
        exception.getMessage(),
        request);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ProblemDetails> handleBadRequest(
      Exception exception, HttpServletRequest request) {
    return problem(ApiProblemSpec.badRequest(), exception.getMessage(), request);
  }

  private ResponseEntity<ProblemDetails> problem(
      ApiProblemSpec spec, String detail, HttpServletRequest request) {
    ProblemDetails body =
        new ProblemDetails(spec.title(), spec.status().value(), spec.code(), spec.params())
            .type(spec.type())
            .detail(detail)
            .instance(URI.create(request.getRequestURI()).toString());

    return ResponseEntity.status(spec.status())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(body);
  }
}
