package dev.dictum.api.settings;

public class InvalidPatchRequestException extends RuntimeException {

  public InvalidPatchRequestException(String message) {
    super(message);
  }
}
