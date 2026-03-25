package dev.dictum.api.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dictum.content")
public class DictumContentProperties {

  private ContentRepositoryType repository = ContentRepositoryType.FILESYSTEM;
  private Path root;

  public ContentRepositoryType getRepository() {
    return repository;
  }

  public void setRepository(ContentRepositoryType repository) {
    this.repository = repository == null ? ContentRepositoryType.FILESYSTEM : repository;
  }

  public Path getRoot() {
    return root;
  }

  public void setRoot(Path root) {
    this.root = root;
  }

  public Path requireRoot() {
    if (root == null) {
      throw new IllegalStateException(
          "Property dictum.content.root must be configured when using the filesystem repository");
    }

    return root;
  }
}
