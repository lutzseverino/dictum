package dev.dictum.api.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class FilesystemContentFixture {

  private FilesystemContentFixture() {}

  public static void writeSeed(Path contentRoot) {
    reset(contentRoot);
    write(contentRoot.resolve("posts/dictum-begins/index.md"), seededPublishedPost());
    write(
        contentRoot.resolve("posts/dictum-begins/style.css"),
        """
        article {
          border-inline-start: 0.4rem solid #dd6b20;
          padding-inline-start: 1.25rem;
        }
        """);
    write(contentRoot.resolve("posts/dictum-begins/meta.json"), "{}");
    write(contentRoot.resolve("posts/remote-controls-later/index.md"), seededDraftPost());
    write(
        contentRoot.resolve("settings/site.json"),
        """
        {"title":"Dictum","subtitle":"A remotely steerable markdown blog kit.","motd":"Foundation mode is live: boundaries first, resources next."}
        """);
  }

  private static void reset(Path contentRoot) {
    try {
      if (Files.exists(contentRoot)) {
        try (Stream<Path> paths = Files.walk(contentRoot)) {
          paths
              .sorted(Comparator.reverseOrder())
              .filter(path -> !path.equals(contentRoot))
              .forEach(FilesystemContentFixture::delete);
        }
      }

      Files.createDirectories(contentRoot);
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to reset filesystem content fixture", exception);
    }
  }

  private static void delete(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to delete fixture path " + path, exception);
    }
  }

  private static void write(Path path, String content) {
    try {
      Files.createDirectories(path.getParent());
      Files.writeString(path, content, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to write fixture path " + path, exception);
    }
  }

  private static String seededPublishedPost() {
    return """
        ---
        title: Dictum Begins
        slug: dictum-begins
        excerpt: A first seeded document proving the site reads through a content service.
        publishedAt: 2026-03-12
        tags:
          - architecture
          - foundation
        template: essay
        status: published
        ---
        Dictum starts life as a hybrid stack with a deliberate split between content, control plane, and presentation.

        The public site already reads through a service abstraction.
        """;
  }

  private static String seededDraftPost() {
    return """
        ---
        title: Remote Controls, Later
        slug: remote-controls-later
        excerpt: The admin experience will later own publish and settings mutations.
        tags:
          - admin
          - control-plane
        template: dispatch
        status: draft
        ---
        Remote editing should stay lightweight.

        The next slices will connect this draft to the generated control-plane client.
        """;
  }
}
