package dev.dictum.api.posts;

public class PostConflictException extends RuntimeException {

  public PostConflictException(String message) {
    super(message);
  }
}
