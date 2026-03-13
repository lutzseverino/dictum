package dev.dictum.api.content.service;

import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.vo.PostSlug;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.content.repository.InMemoryPostStore;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostQueryService {

  private final InMemoryPostStore postStore;
  private final PostApiMapper postApiMapper;

  PostQueryService(InMemoryPostStore postStore, PostApiMapper postApiMapper) {
    this.postStore = postStore;
    this.postApiMapper = postApiMapper;
  }

  public List<PostSummary> listResponses() {
    return postApiMapper.toSummaries(postStore.findAll());
  }

  public PostResponse getResponse(String slug) {
    String validatedSlug = new PostSlug(slug).value();

    PostState state =
        postStore
            .findBySlug(validatedSlug)
            .orElseThrow(
                () -> new PostNotFoundException("No post exists for slug " + validatedSlug));

    return postApiMapper.toResponse(state);
  }
}
