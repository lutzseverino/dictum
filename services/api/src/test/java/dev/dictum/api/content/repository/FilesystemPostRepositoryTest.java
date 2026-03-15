package dev.dictum.api.content.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
    postRepository = new FilesystemPostRepository(contentRoot.toString());
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
}
