package dev.dictum.api.content.store;

import dev.dictum.api.content.model.vo.PostState;
import java.util.List;
import java.util.Optional;

public interface PostStore {

  List<PostState> findAll();

  Optional<PostState> findBySlug(String slug);

  boolean exists(String slug);

  PostState save(PostState state);
}
