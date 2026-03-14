package dev.dictum.api.content.error;

public class PostConflictException extends RuntimeException {

  public PostConflictException(String message) {
    super(message);
  }
}
