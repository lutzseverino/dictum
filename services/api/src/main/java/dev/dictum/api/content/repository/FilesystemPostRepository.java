package dev.dictum.api.content.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.dictum.api.config.FilesystemContentRoot;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FilesystemPostRepository implements PostRepository {

  private static final String POSTS_DIRECTORY = "posts";
  private static final String CONTENT_FILENAME = "index.md";
  private static final String STYLESHEET_FILENAME = "style.css";
  private static final String META_FILENAME = "meta.json";
  private static final String FRONTMATTER_BOUNDARY = "---\n";

  private final Path contentRoot;
  private final Path postsRoot;
  private final YAMLMapper yamlMapper;
  private final ObjectMapper jsonMapper;

  public FilesystemPostRepository(FilesystemContentRoot contentRoot) {
    this.contentRoot = contentRoot.root();
    this.postsRoot = contentRoot.postsRoot();
    this.yamlMapper =
        new YAMLMapper(
            YAMLFactory.builder().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build());
    yamlMapper.findAndRegisterModules();
    yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.jsonMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Override
  public List<PostState> findAll() {
    try (Stream<Path> directories = Files.list(postsRoot)) {
      return directories
          .filter(Files::isDirectory)
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .map(this::readPost)
          .toList();
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to read posts from content repository", exception);
    }
  }

  @Override
  public Optional<PostState> findBySlug(String slug) {
    Path postDirectory = postsRoot.resolve(slug);
    if (!Files.isDirectory(postDirectory)) {
      return Optional.empty();
    }

    return Optional.of(readPost(postDirectory));
  }

  @Override
  public boolean exists(String slug) {
    return Files.isDirectory(postsRoot.resolve(slug));
  }

  @Override
  public PostState save(PostState state) {
    Path postDirectory = postsRoot.resolve(state.slug());

    try {
      Files.createDirectories(postDirectory);
      writeContent(postDirectory.resolve(CONTENT_FILENAME), state);
      writeStylesheet(postDirectory.resolve(STYLESHEET_FILENAME), state);
      writeMeta(postDirectory.resolve(META_FILENAME), state);
      return readPost(postDirectory);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to write post " + state.slug() + " to content repository", exception);
    }
  }

  private PostState readPost(Path postDirectory) {
    Path contentPath = postDirectory.resolve(CONTENT_FILENAME);
    Path stylesheetPath = postDirectory.resolve(STYLESHEET_FILENAME);
    Path metaPath = postDirectory.resolve(META_FILENAME);

    try {
      MarkdownPostDocument document = parseMarkdown(Files.readString(contentPath));
      String directorySlug = postDirectory.getFileName().toString();
      validateDirectorySlug(directorySlug, document.frontmatter().slug());
      String stylesheetContent =
          Files.exists(stylesheetPath) ? Files.readString(stylesheetPath) : null;

      return new PostState(
          directorySlug,
          document.frontmatter().title(),
          document.frontmatter().excerpt(),
          document.frontmatter().status(),
          document.frontmatter().template(),
          document.frontmatter().publishedAt(),
          List.copyOf(document.frontmatter().tags()),
          stylesheetContent != null,
          document.body(),
          stylesheetContent,
          relativePath(contentPath),
          stylesheetContent != null ? relativePath(stylesheetPath) : null,
          Files.exists(metaPath) ? relativePath(metaPath) : null);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Failed to read post " + postDirectory.getFileName() + " from content repository",
          exception);
    }
  }

  private MarkdownPostDocument parseMarkdown(String markdown) throws JsonProcessingException {
    String normalizedMarkdown = markdown.replace("\r\n", "\n");

    if (!normalizedMarkdown.startsWith(FRONTMATTER_BOUNDARY)) {
      throw new IllegalStateException("Markdown content is missing YAML frontmatter");
    }

    int closingBoundaryIndex = normalizedMarkdown.indexOf("\n---\n", FRONTMATTER_BOUNDARY.length());
    if (closingBoundaryIndex < 0) {
      throw new IllegalStateException(
          "Markdown content has an unterminated YAML frontmatter block");
    }

    String frontmatter =
        normalizedMarkdown.substring(FRONTMATTER_BOUNDARY.length(), closingBoundaryIndex);
    String body = normalizedMarkdown.substring(closingBoundaryIndex + "\n---\n".length());

    return new MarkdownPostDocument(
        yamlMapper.readValue(frontmatter, MarkdownPostFrontmatter.class), body);
  }

  private void writeContent(Path contentPath, PostState state) throws IOException {
    MarkdownPostFrontmatter frontmatter =
        new MarkdownPostFrontmatter(
            state.title(),
            state.slug(),
            state.excerpt(),
            state.publishedAt(),
            state.tags(),
            state.template(),
            state.status());

    StringBuilder markdown = new StringBuilder();
    markdown.append(FRONTMATTER_BOUNDARY);
    markdown.append(yamlMapper.writeValueAsString(frontmatter));
    markdown.append(FRONTMATTER_BOUNDARY);
    markdown.append(state.body());

    Files.writeString(contentPath, markdown.toString(), StandardCharsets.UTF_8);
  }

  private void writeStylesheet(Path stylesheetPath, PostState state) throws IOException {
    if (state.stylesheetContent() == null) {
      Files.deleteIfExists(stylesheetPath);
      return;
    }

    Files.writeString(stylesheetPath, state.stylesheetContent(), StandardCharsets.UTF_8);
  }

  private void writeMeta(Path metaPath, PostState state) throws IOException {
    if (state.metaPath() == null) {
      Files.deleteIfExists(metaPath);
      return;
    }

    if (Files.exists(metaPath)) {
      return;
    }

    Files.writeString(
        metaPath, jsonMapper.writeValueAsString(java.util.Map.of()), StandardCharsets.UTF_8);
  }

  private String relativePath(Path file) {
    return contentRoot.relativize(file).toString().replace('\\', '/');
  }

  private void validateDirectorySlug(String directorySlug, String frontmatterSlug) {
    if (!directorySlug.equals(frontmatterSlug)) {
      throw new IllegalStateException(
          "Post frontmatter slug "
              + frontmatterSlug
              + " does not match directory slug "
              + directorySlug);
    }
  }

  private record MarkdownPostDocument(MarkdownPostFrontmatter frontmatter, String body) {}

  private record MarkdownPostFrontmatter(
      String title,
      String slug,
      String excerpt,
      LocalDate publishedAt,
      List<String> tags,
      PostTemplate template,
      PostStatus status) {}
}
