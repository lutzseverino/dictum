package dev.dictum.api.content.service;

import dev.dictum.api.content.command.CreatePostCommand;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.vo.PostPatch;
import dev.dictum.api.content.model.vo.PostSlug;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.content.model.vo.PostTags;
import dev.dictum.api.content.store.PostStore;
import dev.dictum.api.generated.model.PostStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PostCommandService {

  private static final String POSTS_DIRECTORY = "posts/";
  private static final String INDEX_FILENAME = "index.md";
  private static final String STYLESHEET_FILENAME = "style.css";
  private static final String META_FILENAME = "meta.json";

  private final PostStore postStore;

  PostCommandService(PostStore postStore) {
    this.postStore = postStore;
  }

  public PostState create(CreatePostCommand command) {
    String slug = new PostSlug(command.slug()).value();

    if (postStore.exists(slug)) {
      throw new PostConflictException("A post already exists for slug " + slug);
    }

    PostState created =
        new PostState(
            slug,
            command.title(),
            command.excerpt(),
            PostStatus.DRAFT,
            command.template(),
            null,
            new PostTags(command.tags()).values(),
            command.stylesheet() != null,
            command.body(),
            command.stylesheet(),
            contentPathFor(slug),
            command.stylesheet() != null ? stylesheetPathFor(slug) : null,
            metaPathFor(slug));

    return postStore.save(created);
  }

  public PostState update(String slug, PostPatch patch) {
    PostState current = requireState(slug);
    patch.validate();

    return postStore.save(
        patch.applyTo(current, resolveStylesheetPath(slug, current.stylesheetPath(), patch)));
  }

  public PostState publish(String slug) {
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

    return postStore.save(published);
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

  private String resolveStylesheetPath(String slug, String currentStylesheetPath, PostPatch patch) {
    if (Boolean.TRUE.equals(patch.removeStylesheet().value())) {
      return null;
    }

    if (patch.stylesheet().isPresent()) {
      return patch.stylesheet().value() != null ? stylesheetPathFor(slug) : null;
    }

    return currentStylesheetPath;
  }

  private String postAssetPath(String slug, String filename) {
    return POSTS_DIRECTORY + slug + "/" + filename;
  }
}
