package dev.dictum.api.content.controller;

import dev.dictum.api.content.service.PostCommandService;
import dev.dictum.api.content.service.PostQueryService;
import dev.dictum.api.generated.api.PostsApi;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import dev.dictum.api.generated.model.UpdatePostRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Validated
public class PostsController implements PostsApi {

  private final PostQueryService postQueryService;
  private final PostCommandService postCommandService;

  PostsController(PostQueryService postQueryService, PostCommandService postCommandService) {
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
    return ResponseEntity.created(
            UriComponentsBuilder.fromPath("/api/v1/posts/{slug}")
                .buildAndExpand(response.getSlug())
                .toUri())
        .body(response);
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
