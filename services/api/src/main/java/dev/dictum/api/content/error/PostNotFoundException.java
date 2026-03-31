package dev.dictum.api.content.error;

public class PostNotFoundException extends RuntimeException {

  private final String slug;

  public PostNotFoundException(String slug) {
    super("No post exists for slug " + slug);
    this.slug = slug;
  }

  public String slug() {
    return slug;
  }
}
