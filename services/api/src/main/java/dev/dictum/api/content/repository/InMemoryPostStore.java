package dev.dictum.api.content.repository;

import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InMemoryPostStore {

  private final Map<String, PostState> posts = new LinkedHashMap<>();

  InMemoryPostStore() {
    PostState firstPost =
        new PostState(
            "dictum-begins",
            "Dictum Begins",
            "A first seeded document proving the site reads through a content service.",
            PostStatus.PUBLISHED,
            PostTemplate.ESSAY,
            LocalDate.parse("2026-03-12"),
            List.of("architecture", "foundation"),
            true,
            """
            Dictum starts life as a hybrid stack with a deliberate split between content, control plane, and presentation.

            The public site already reads through a service abstraction.
            """,
            "posts/dictum-begins/index.md",
            "posts/dictum-begins/style.css",
            "posts/dictum-begins/meta.json");

    PostState secondPost =
        new PostState(
            "remote-controls-later",
            "Remote Controls, Later",
            "The admin experience will later own publish and settings mutations.",
            PostStatus.DRAFT,
            PostTemplate.DISPATCH,
            null,
            List.of("admin", "control-plane"),
            false,
            """
            Remote editing should stay lightweight.

            The next slices will connect this draft to the generated control-plane client.
            """,
            "posts/remote-controls-later/index.md",
            null,
            null);

    posts.put(firstPost.slug(), firstPost);
    posts.put(secondPost.slug(), secondPost);
  }

  public synchronized List<PostState> findAll() {
    return new ArrayList<>(posts.values());
  }

  public synchronized Optional<PostState> findBySlug(String slug) {
    return Optional.ofNullable(posts.get(slug));
  }

  public synchronized boolean exists(String slug) {
    return posts.containsKey(slug);
  }

  public synchronized PostState save(PostState state) {
    posts.put(state.slug(), state);
    return state;
  }
}
