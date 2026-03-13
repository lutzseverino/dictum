package dev.dictum.api.posts;

import dev.dictum.api.api.MergePatchBodyAccessor;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.settings.InvalidPatchRequestException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
class PostCommandService {

  private static final String POSTS_DIRECTORY = "posts/";
  private static final String INDEX_FILENAME = "index.md";
  private static final String STYLESHEET_FILENAME = "style.css";
  private static final String META_FILENAME = "meta.json";

  private final InMemoryPostStore postStore;
  private final PostApiMapper postApiMapper;
  private final MergePatchBodyAccessor mergePatchBodyAccessor;

  PostCommandService(
      InMemoryPostStore postStore,
      PostApiMapper postApiMapper,
      MergePatchBodyAccessor mergePatchBodyAccessor) {
    this.postStore = postStore;
    this.postApiMapper = postApiMapper;
    this.mergePatchBodyAccessor = mergePatchBodyAccessor;
  }

  public PostResponse create(CreatePostRequest request) {
    String slug = PostInputRules.requireValidSlug(request.getSlug());

    if (postStore.exists(slug)) {
      throw new PostConflictException("A post already exists for slug " + slug);
    }

    PostState created =
        new PostState(
            slug,
            request.getTitle(),
            request.getExcerpt(),
            PostStatus.DRAFT,
            request.getTemplate(),
            null,
            PostInputRules.copyTags("tags", request.getTags()),
            request.getStylesheet() != null,
            request.getBody(),
            contentPathFor(slug),
            request.getStylesheet() != null ? stylesheetPathFor(slug) : null,
            metaPathFor(slug));

    return postApiMapper.toResponse(postStore.save(created));
  }

  public PostResponse update(String slug, UpdatePostRequest request) {
    PostState current = requireState(slug);
    PostPatchFields patchFields = readPatchFields();
    validateUpdateRequest(request, patchFields);

    return postApiMapper.toResponse(
        postStore.save(updatedState(slug, current, request, patchFields)));
  }

  public PostResponse publish(String slug) {
    PostState current = requireState(slug);

    if (current.status() == PostStatus.PUBLISHED) {
      throw new PostConflictException("Post " + slug + " is already published");
    }

    PostState published =
        new PostState(
            current.slug(),
            current.title(),
            current.excerpt(),
            PostStatus.PUBLISHED,
            current.template(),
            LocalDate.now(),
            current.tags(),
            current.hasStylesheet(),
            current.body(),
            current.contentPath(),
            current.stylesheetPath(),
            current.metaPath());

    return postApiMapper.toResponse(postStore.save(published));
  }

  private PostState requireState(String slug) {
    PostInputRules.requireValidSlug(slug);

    return postStore
        .findBySlug(slug)
        .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + slug));
  }

  private String contentPathFor(String slug) {
    return postAssetPath(slug, INDEX_FILENAME);
  }

  private String stylesheetPathFor(String slug) {
    return postAssetPath(slug, STYLESHEET_FILENAME);
  }

  private String metaPathFor(String slug) {
    return postAssetPath(slug, META_FILENAME);
  }

  private PostPatchFields readPatchFields() {
    mergePatchBodyAccessor.requireAnyField();

    return new PostPatchFields(
        mergePatchBodyAccessor.containsField("title"),
        mergePatchBodyAccessor.containsField("excerpt"),
        mergePatchBodyAccessor.containsField("template"),
        mergePatchBodyAccessor.containsField("tags"),
        mergePatchBodyAccessor.containsField("body"),
        mergePatchBodyAccessor.containsField("stylesheet"),
        mergePatchBodyAccessor.containsField("removeStylesheet"));
  }

  private void validateUpdateRequest(UpdatePostRequest request, PostPatchFields patchFields) {
    requireNonNullWhenPresent("title", patchFields.title(), request.getTitle());
    requireNonNullWhenPresent("excerpt", patchFields.excerpt(), request.getExcerpt());
    requireNonNullWhenPresent("template", patchFields.template(), request.getTemplate());
    requireNonNullWhenPresent("body", patchFields.body(), request.getBody());
    requireNonNullWhenPresent(
        "removeStylesheet", patchFields.removeStylesheet(), request.getRemoveStylesheet());

    if (patchFields.tags() && mergePatchBodyAccessor.isExplicitNull("tags")) {
      throw new InvalidPatchRequestException("Field tags cannot be null");
    }
  }

  private void requireNonNullWhenPresent(String fieldName, boolean fieldPresent, Object value) {
    if (fieldPresent && value == null) {
      throw new InvalidPatchRequestException("Field " + fieldName + " cannot be null");
    }
  }

  private PostState updatedState(
      String slug, PostState current, UpdatePostRequest request, PostPatchFields patchFields) {
    String stylesheetPath =
        resolveStylesheetPath(slug, current.stylesheetPath(), request, patchFields);

    return new PostState(
        current.slug(),
        patchFields.title() ? request.getTitle() : current.title(),
        patchFields.excerpt() ? request.getExcerpt() : current.excerpt(),
        current.status(),
        patchFields.template() ? request.getTemplate() : current.template(),
        current.publishedAt(),
        patchFields.tags() ? PostInputRules.copyTags("tags", request.getTags()) : current.tags(),
        stylesheetPath != null,
        patchFields.body() ? request.getBody() : current.body(),
        current.contentPath(),
        stylesheetPath,
        current.metaPath());
  }

  private String resolveStylesheetPath(
      String slug,
      String currentStylesheetPath,
      UpdatePostRequest request,
      PostPatchFields patchFields) {
    if (Boolean.TRUE.equals(request.getRemoveStylesheet())) {
      return null;
    }

    if (patchFields.stylesheet()) {
      return request.getStylesheet() != null ? stylesheetPathFor(slug) : null;
    }

    if (request.getStylesheet() != null) {
      return stylesheetPathFor(slug);
    }

    return currentStylesheetPath;
  }

  private String postAssetPath(String slug, String filename) {
    return POSTS_DIRECTORY + slug + "/" + filename;
  }

  private record PostPatchFields(
      boolean title,
      boolean excerpt,
      boolean template,
      boolean tags,
      boolean body,
      boolean stylesheet,
      boolean removeStylesheet) {}
}
