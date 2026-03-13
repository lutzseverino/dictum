package dev.dictum.api.posts;

public class PostNotFoundException extends RuntimeException {

  public PostNotFoundException(String message) {
    super(message);
  }
}
