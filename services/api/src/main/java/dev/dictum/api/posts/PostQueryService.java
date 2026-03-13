package dev.dictum.api.posts;

import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class PostQueryService {

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
    PostInputRules.requireValidSlug(slug);

    PostState state =
        postStore
            .findBySlug(slug)
            .orElseThrow(() -> new PostNotFoundException("No post exists for slug " + slug));

    return postApiMapper.toResponse(state);
  }
}
