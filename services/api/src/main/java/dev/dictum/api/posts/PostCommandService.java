package dev.dictum.api.posts;

import dev.dictum.api.api.MergePatchBodyAccessor;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.settings.InvalidPatchRequestException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostCommandService {

  private final InMemoryPostStore postStore;
  private final PostApiMapper postApiMapper;
  private final MergePatchBodyAccessor mergePatchBodyAccessor;

  public PostCommandService(
      InMemoryPostStore postStore,
      PostApiMapper postApiMapper,
      MergePatchBodyAccessor mergePatchBodyAccessor) {
    this.postStore = postStore;
    this.postApiMapper = postApiMapper;
    this.mergePatchBodyAccessor = mergePatchBodyAccessor;
  }

  public PostResponse create(CreatePostRequest request) {
    if (postStore.exists(request.getSlug())) {
      throw new PostConflictException("A post already exists for slug " + request.getSlug());
    }

    PostState created =
        new PostState(
            request.getSlug(),
            request.getTitle(),
            request.getExcerpt(),
            PostStatus.DRAFT,
            request.getTemplate(),
            null,
            List.copyOf(request.getTags()),
            request.getStylesheet() != null,
            request.getBody(),
            contentPathFor(request.getSlug()),
            request.getStylesheet() != null ? stylesheetPathFor(request.getSlug()) : null,
            metaPathFor(request.getSlug()));

    return postApiMapper.toResponse(postStore.save(created));
  }

  public PostResponse update(String slug, UpdatePostRequest request) {
    PostState current = requireState(slug);
    mergePatchBodyAccessor.requireAnyField();
    String stylesheetPath = current.stylesheetPath();

    if (mergePatchBodyAccessor.containsField("title") && request.getTitle() == null) {
      throw new InvalidPatchRequestException("Field title cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("excerpt") && request.getExcerpt() == null) {
      throw new InvalidPatchRequestException("Field excerpt cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("template") && request.getTemplate() == null) {
      throw new InvalidPatchRequestException("Field template cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("body") && request.getBody() == null) {
      throw new InvalidPatchRequestException("Field body cannot be null");
    }

    if (mergePatchBodyAccessor.containsField("tags")
        && mergePatchBodyAccessor.isExplicitNull("tags")) {
      throw new InvalidPatchRequestException("Field tags cannot be null");
    }

    if (Boolean.TRUE.equals(request.getRemoveStylesheet())) {
      stylesheetPath = null;
    } else if (mergePatchBodyAccessor.containsField("stylesheet")) {
      stylesheetPath = request.getStylesheet() != null ? stylesheetPathFor(slug) : null;
    } else if (request.getStylesheet() != null) {
      stylesheetPath = stylesheetPathFor(slug);
    }

    PostState updated =
        new PostState(
            current.slug(),
            mergePatchBodyAccessor.containsField("title") ? request.getTitle() : current.title(),
            mergePatchBodyAccessor.containsField("excerpt")
                ? request.getExcerpt()
                : current.excerpt(),
            current.status(),
            mergePatchBodyAccessor.containsField("template")
                ? request.getTemplate()
                : current.template(),
            current.publishedAt(),
            mergePatchBodyAccessor.containsField("tags")
                ? List.copyOf(request.getTags())
                : current.tags(),
            stylesheetPath != null,
            mergePatchBodyAccessor.containsField("body") ? request.getBody() : current.body(),
            current.contentPath(),
            stylesheetPath,
            current.metaPath());

    return postApiMapper.toResponse(postStore.save(updated));
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
    return postStore
        .findBySlug(slug)
        .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + slug));
  }

  private String contentPathFor(String slug) {
    return "posts/" + slug + "/index.md";
  }

  private String stylesheetPathFor(String slug) {
    return "posts/" + slug + "/style.css";
  }

  private String metaPathFor(String slug) {
    return "posts/" + slug + "/meta.json";
  }
}
