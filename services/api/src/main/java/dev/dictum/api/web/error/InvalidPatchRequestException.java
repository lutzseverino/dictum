package dev.dictum.api.web.error;

public class InvalidPatchRequestException extends RuntimeException {

  public InvalidPatchRequestException(String message) {
    super(message);
  }
}
