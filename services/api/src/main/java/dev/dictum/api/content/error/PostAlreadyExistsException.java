package dev.dictum.api.content.error;

public class PostAlreadyExistsException extends RuntimeException {

  private final String slug;

  public PostAlreadyExistsException(String slug) {
    super("A post already exists for slug " + slug);
    this.slug = slug;
  }

  public String slug() {
    return slug;
  }
}
