package dev.dictum.api.config;

import java.nio.file.Files;
import java.nio.file.Path;

public record FilesystemContentRoot(Path root, Path postsRoot, Path siteSettingsFile) {

  public static FilesystemContentRoot from(Path root) {
    if (root == null) {
      throw new IllegalStateException(
          "Property dictum.content.root must be configured when using the filesystem repository");
    }

    Path postsRoot = root.resolve("posts");
    Path siteSettingsFile = root.resolve("settings").resolve("site.json");

    if (!Files.isDirectory(postsRoot)) {
      throw new IllegalStateException("Content repository is missing posts/");
    }

    if (!Files.isRegularFile(siteSettingsFile)) {
      throw new IllegalStateException("Content repository is missing settings/site.json");
    }

    return new FilesystemContentRoot(root, postsRoot, siteSettingsFile);
  }
}
