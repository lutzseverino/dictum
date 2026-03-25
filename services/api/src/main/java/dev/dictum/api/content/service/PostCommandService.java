package dev.dictum.api.content.service;

import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.mapper.PostApiMapper;
import dev.dictum.api.content.model.vo.PostPatch;
import dev.dictum.api.content.model.vo.PostSlug;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.content.model.vo.PostTags;
import dev.dictum.api.content.rule.PostPatchValidator;
import dev.dictum.api.content.store.PostStore;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PostCommandService {

  private static final String POSTS_DIRECTORY = "posts/";
  private static final String INDEX_FILENAME = "index.md";
  private static final String STYLESHEET_FILENAME = "style.css";
  private static final String META_FILENAME = "meta.json";

  private final PostStore postStore;
  private final PostApiMapper postApiMapper;
  private final PostPatchValidator postPatchValidator;
  private final MergePatchDocumentAccessor mergePatchDocumentAccessor;

  PostCommandService(
      PostStore postStore,
      PostApiMapper postApiMapper,
      PostPatchValidator postPatchValidator,
      MergePatchDocumentAccessor mergePatchDocumentAccessor) {
    this.postStore = postStore;
    this.postApiMapper = postApiMapper;
    this.postPatchValidator = postPatchValidator;
    this.mergePatchDocumentAccessor = mergePatchDocumentAccessor;
  }

  public PostResponse create(CreatePostRequest request) {
    String slug = new PostSlug(request.getSlug()).value();

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
            new PostTags(request.getTags()).values(),
            request.getStylesheet() != null,
            request.getBody(),
            request.getStylesheet(),
            contentPathFor(slug),
            request.getStylesheet() != null ? stylesheetPathFor(slug) : null,
            metaPathFor(slug));

    return postApiMapper.toResponse(postStore.save(created));
  }

  public PostResponse update(String slug, UpdatePostRequest request) {
    PostState current = requireState(slug);
    PostPatch patch = readPatch(request);
    postPatchValidator.validate(patch);

    return postApiMapper.toResponse(postStore.save(updatedState(slug, current, patch)));
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
            current.stylesheetContent(),
            current.contentPath(),
            current.stylesheetPath(),
            current.metaPath());

    return postApiMapper.toResponse(postStore.save(published));
  }

  private PostState requireState(String slug) {
    String validatedSlug = new PostSlug(slug).value();

    return postStore
        .findBySlug(validatedSlug)
        .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + validatedSlug));
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

  private PostPatch readPatch(UpdatePostRequest request) {
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();
    return PostPatch.from(request, document);
  }

  private PostState updatedState(String slug, PostState current, PostPatch patch) {
    String stylesheetPath = resolveStylesheetPath(slug, current.stylesheetPath(), patch);

    return new PostState(
        current.slug(),
        patch.title().isPresent() ? patch.title().value() : current.title(),
        patch.excerpt().isPresent() ? patch.excerpt().value() : current.excerpt(),
        current.status(),
        patch.template().isPresent() ? patch.template().value() : current.template(),
        current.publishedAt(),
        patch.tags().isPresent() ? new PostTags(patch.tags().value()).values() : current.tags(),
        stylesheetPath != null,
        patch.body().isPresent() ? patch.body().value() : current.body(),
        resolveStylesheetContent(current.stylesheetContent(), patch),
        current.contentPath(),
        stylesheetPath,
        current.metaPath());
  }

  private String resolveStylesheetPath(String slug, String currentStylesheetPath, PostPatch patch) {
    if (Boolean.TRUE.equals(patch.removeStylesheet().value())) {
      return null;
    }

    if (patch.stylesheet().isPresent()) {
      return patch.stylesheet().value() != null ? stylesheetPathFor(slug) : null;
    }

    return currentStylesheetPath;
  }

  private String resolveStylesheetContent(String currentStylesheetContent, PostPatch patch) {
    if (Boolean.TRUE.equals(patch.removeStylesheet().value())) {
      return null;
    }

    if (patch.stylesheet().isPresent()) {
      return patch.stylesheet().value();
    }

    return currentStylesheetContent;
  }

  private String postAssetPath(String slug, String filename) {
    return POSTS_DIRECTORY + slug + "/" + filename;
  }
}
