package dev.dictum.api.posts;

import dev.dictum.api.generated.api.PostsApi;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import dev.dictum.api.generated.model.UpdatePostRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostsController implements PostsApi {

  private final PostQueryService postQueryService;
  private final PostCommandService postCommandService;

  public PostsController(PostQueryService postQueryService, PostCommandService postCommandService) {
    this.postQueryService = postQueryService;
    this.postCommandService = postCommandService;
  }

  @Override
  public ResponseEntity<List<PostSummary>> listPosts() {
    return ResponseEntity.ok(postQueryService.listResponses());
  }

  @Override
  public ResponseEntity<PostResponse> createPost(CreatePostRequest createPostRequest) {
    PostResponse response = postCommandService.create(createPostRequest);
    return ResponseEntity.created(URI.create("/api/v1/posts/" + response.getSlug())).body(response);
  }

  @Override
  public ResponseEntity<PostResponse> getPost(String slug) {
    return ResponseEntity.ok(postQueryService.getResponse(slug));
  }

  @Override
  public ResponseEntity<PostResponse> updatePost(String slug, UpdatePostRequest updatePostRequest) {
    return ResponseEntity.ok(postCommandService.update(slug, updatePostRequest));
  }

  @Override
  public ResponseEntity<PostResponse> publishPost(String slug) {
    return ResponseEntity.ok(postCommandService.publish(slug));
  }
}
