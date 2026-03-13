package dev.dictum.api.posts;

public class InvalidPostRequestException extends RuntimeException {

  public InvalidPostRequestException(String message) {
    super(message);
  }
}
