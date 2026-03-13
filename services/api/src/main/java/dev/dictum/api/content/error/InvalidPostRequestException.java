package dev.dictum.api.content.error;

public class InvalidPostRequestException extends RuntimeException {

  public InvalidPostRequestException(String message) {
    super(message);
  }
}
