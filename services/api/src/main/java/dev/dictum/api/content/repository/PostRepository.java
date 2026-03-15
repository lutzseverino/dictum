package dev.dictum.api.content.repository;

import dev.dictum.api.content.model.vo.PostState;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

  List<PostState> findAll();

  Optional<PostState> findBySlug(String slug);

  boolean exists(String slug);

  PostState save(PostState state);
}
