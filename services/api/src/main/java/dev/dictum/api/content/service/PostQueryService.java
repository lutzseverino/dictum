package dev.dictum.api.content.service;

import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.state.PostState;
import dev.dictum.api.content.model.vo.PostSlug;
import dev.dictum.api.content.store.PostStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostQueryService {

  private final PostStore postStore;

  PostQueryService(PostStore postStore) {
    this.postStore = postStore;
  }

  public List<PostState> list() {
    return postStore.findAll();
  }

  public PostState get(String slug) {
    String validatedSlug = new PostSlug(slug).value();

    return postStore
        .findBySlug(validatedSlug)
        .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + validatedSlug));
  }
}
