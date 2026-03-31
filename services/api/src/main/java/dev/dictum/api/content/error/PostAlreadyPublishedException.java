package dev.dictum.api.content.error;

public class PostAlreadyPublishedException extends RuntimeException {

  private final String slug;

  public PostAlreadyPublishedException(String slug) {
    super("Post " + slug + " is already published");
    this.slug = slug;
  }

  public String slug() {
    return slug;
  }
}
