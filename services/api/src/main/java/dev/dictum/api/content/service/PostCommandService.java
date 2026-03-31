package dev.dictum.api.content.service;

import dev.dictum.api.content.command.CreatePostCommand;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.patch.PostPatch;
import dev.dictum.api.content.model.state.PostState;
import dev.dictum.api.content.model.vo.PostSlug;
import dev.dictum.api.content.model.vo.PostStatus;
import dev.dictum.api.content.model.vo.PostTags;
import dev.dictum.api.content.store.PostStore;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PostCommandService {

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
            command.stylesheet());

    return postStore.save(created);
  }

  public PostState update(String slug, PostPatch patch) {
    PostState current = requireState(slug);
    patch.validate();

    return postStore.save(patch.applyTo(current));
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
            current.stylesheetContent());

    return postStore.save(published);
  }

  private PostState requireState(String slug) {
    String validatedSlug = new PostSlug(slug).value();

    return postStore
        .findBySlug(validatedSlug)
        .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + validatedSlug));
  }
}
