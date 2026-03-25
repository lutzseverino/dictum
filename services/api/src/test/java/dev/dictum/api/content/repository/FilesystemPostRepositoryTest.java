package dev.dictum.api.content.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.config.DictumContentProperties;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import dev.dictum.api.support.FilesystemContentFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemPostRepositoryTest {

  @TempDir private Path contentRoot;

  private FilesystemPostRepository postRepository;

  @BeforeEach
  void setUp() {
    FilesystemContentFixture.writeSeed(contentRoot);
    postRepository = new FilesystemPostRepository(contentProperties());
  }

  private DictumContentProperties contentProperties() {
    DictumContentProperties contentProperties = new DictumContentProperties();
    contentProperties.setRoot(contentRoot);
    return contentProperties;
  }

  @Test
  void findAllReadsMarkdownFrontmatterAndStylesheetContent() {
    List<PostState> posts = postRepository.findAll();

    assertThat(posts).hasSize(2);
    assertThat(posts.get(0).slug()).isEqualTo("dictum-begins");
    assertThat(posts.get(0).publishedAt()).isEqualTo(LocalDate.parse("2026-03-12"));
    assertThat(posts.get(0).stylesheetContent()).contains("border-inline-start");
    assertThat(posts.get(1).slug()).isEqualTo("remote-controls-later");
    assertThat(posts.get(1).stylesheetContent()).isNull();
  }

  @Test
  void savePersistsPostStateBackToFilesystem() throws Exception {
    PostState created =
        new PostState(
            "notes-on-remote-editing",
            "Notes on Remote Editing",
            "First thoughts on a phone-first publishing workflow.",
            PostStatus.DRAFT,
            PostTemplate.NOTE,
            null,
            List.of("product", "mobile"),
            true,
            "Remote editing should stay lightweight.",
            "body { color: tomato; }",
            "posts/notes-on-remote-editing/index.md",
            "posts/notes-on-remote-editing/style.css",
            "posts/notes-on-remote-editing/meta.json");

    PostState saved = postRepository.save(created);

    assertThat(saved.slug()).isEqualTo("notes-on-remote-editing");
    assertThat(saved.stylesheetContent()).isEqualTo("body { color: tomato; }");
    assertThat(Files.readString(contentRoot.resolve("posts/notes-on-remote-editing/index.md")))
        .contains("slug: \"notes-on-remote-editing\"")
        .doesNotContain("publishedAt:")
        .contains("status: \"draft\"")
        .contains("Remote editing should stay lightweight.");
    assertThat(Files.readString(contentRoot.resolve("posts/notes-on-remote-editing/style.css")))
        .isEqualTo("body { color: tomato; }");
    assertThat(Files.readString(contentRoot.resolve("posts/notes-on-remote-editing/meta.json")))
        .isEqualTo("{}");
  }

  @Test
  void savePreservesExistingMetaJsonContent() throws Exception {
    Path metaPath = contentRoot.resolve("posts/dictum-begins/meta.json");
    Files.writeString(metaPath, "{\"motif\":\"accent-border\"}");

    PostState existing = postRepository.findBySlug("dictum-begins").orElseThrow();
    PostState updated =
        new PostState(
            existing.slug(),
            "Dictum Begins, Revised",
            existing.excerpt(),
            existing.status(),
            existing.template(),
            existing.publishedAt(),
            existing.tags(),
            existing.hasStylesheet(),
            existing.body(),
            existing.stylesheetContent(),
            existing.contentPath(),
            existing.stylesheetPath(),
            existing.metaPath());

    postRepository.save(updated);

    assertThat(Files.readString(metaPath)).isEqualTo("{\"motif\":\"accent-border\"}");
  }

  @Test
  void findBySlugRejectsFrontmatterSlugMismatches() throws Exception {
    Files.writeString(
        contentRoot.resolve("posts/dictum-begins/index.md"),
        """
        ---
        title: Dictum Begins
        slug: wrong-slug
        excerpt: A first seeded document proving the site reads through a content service.
        publishedAt: 2026-03-12
        tags:
          - architecture
          - foundation
        template: essay
        status: published
        ---
        Dictum starts life as a hybrid stack.
        """);

    assertThatThrownBy(() -> postRepository.findBySlug("dictum-begins"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Post frontmatter slug wrong-slug does not match directory slug dictum-begins");
  }
}
