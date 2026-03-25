package dev.dictum.api.content.store;

import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryPostStore implements PostStore {

  private final Map<String, PostState> posts = new LinkedHashMap<>();

  public InMemoryPostStore() {
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
            """
            article {
              border-inline-start: 0.4rem solid #dd6b20;
              padding-inline-start: 1.25rem;
            }
            """);

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
            null);

    posts.put(firstPost.slug(), firstPost);
    posts.put(secondPost.slug(), secondPost);
  }

  @Override
  public synchronized List<PostState> findAll() {
    return new ArrayList<>(posts.values());
  }

  @Override
  public synchronized Optional<PostState> findBySlug(String slug) {
    return Optional.ofNullable(posts.get(slug));
  }

  @Override
  public synchronized boolean exists(String slug) {
    return posts.containsKey(slug);
  }

  @Override
  public synchronized PostState save(PostState state) {
    posts.put(state.slug(), state);
    return state;
  }
}
