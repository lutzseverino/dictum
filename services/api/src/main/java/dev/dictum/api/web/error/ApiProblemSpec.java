package dev.dictum.api.web.error;

import dev.dictum.api.auth.error.InvalidCredentialsException;
import dev.dictum.api.content.error.PostAlreadyExistsException;
import dev.dictum.api.content.error.PostAlreadyPublishedException;
import dev.dictum.api.content.error.PostNotFoundException;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

record ApiProblemSpec(
    HttpStatus status, String type, String title, String code, Map<String, Object> params) {

  private static final String PROBLEM_BASE_URL = "https://dictum.dev/problems/";

  ApiProblemSpec {
    params = Map.copyOf(params);
  }

  static ApiProblemSpec postNotFound(PostNotFoundException exception) {
    return new ApiProblemSpec(
        HttpStatus.NOT_FOUND,
        problemType("post-not-found"),
        "Resource not found",
        "post.not_found",
        Map.of("slug", exception.slug()));
  }

  static ApiProblemSpec postAlreadyExists(PostAlreadyExistsException exception) {
    return new ApiProblemSpec(
        HttpStatus.CONFLICT,
        problemType("post-already-exists"),
        "Conflict",
        "post.already_exists",
        Map.of("slug", exception.slug()));
  }

  static ApiProblemSpec postAlreadyPublished(PostAlreadyPublishedException exception) {
    return new ApiProblemSpec(
        HttpStatus.CONFLICT,
        problemType("post-already-published"),
        "Conflict",
        "post.already_published",
        Map.of("slug", exception.slug()));
  }

  static ApiProblemSpec invalidPatchRequest() {
    return new ApiProblemSpec(
        HttpStatus.BAD_REQUEST,
        problemType("invalid-patch-request"),
        "Bad request",
        "patch.invalid",
        Map.of());
  }

  static ApiProblemSpec invalidPostRequest() {
    return new ApiProblemSpec(
        HttpStatus.BAD_REQUEST,
        problemType("invalid-post-request"),
        "Bad request",
        "post.invalid",
        Map.of());
  }

  static ApiProblemSpec unsupportedMediaType(@Nullable MediaType contentType) {
    return new ApiProblemSpec(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        problemType("unsupported-media-type"),
        "Unsupported media type",
        "request.unsupported_media_type",
        contentType == null ? Map.of() : Map.of("contentType", contentType.toString()));
  }

  static ApiProblemSpec badRequest() {
    return new ApiProblemSpec(
        HttpStatus.BAD_REQUEST,
        problemType("bad-request"),
        "Bad request",
        "request.invalid",
        Map.of());
  }

  static ApiProblemSpec invalidCredentials(InvalidCredentialsException exception) {
    return new ApiProblemSpec(
        HttpStatus.UNAUTHORIZED,
        problemType("invalid-credentials"),
        "Authentication failed",
        "auth.invalid_credentials",
        Map.of());
  }

  static ApiProblemSpec unauthenticated() {
    return new ApiProblemSpec(
        HttpStatus.UNAUTHORIZED,
        problemType("unauthenticated"),
        "Authentication required",
        "auth.unauthenticated",
        Map.of());
  }

  static ApiProblemSpec forbidden() {
    return new ApiProblemSpec(
        HttpStatus.FORBIDDEN, problemType("forbidden"), "Forbidden", "auth.forbidden", Map.of());
  }

  static ApiProblemSpec csrfInvalid() {
    return new ApiProblemSpec(
        HttpStatus.FORBIDDEN,
        problemType("invalid-csrf-token"),
        "Forbidden",
        "request.csrf_invalid",
        Map.of());
  }

  private static String problemType(String slug) {
    return PROBLEM_BASE_URL + slug;
  }
}
