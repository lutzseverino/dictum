package dev.dictum.api.content.controller;

import dev.dictum.api.content.factory.PostApiInputFactory;
import dev.dictum.api.content.mapper.PostApiMapper;
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

  private final PostApiInputFactory postApiInputFactory;
  private final PostApiMapper postApiMapper;
  private final PostQueryService postQueryService;
  private final PostCommandService postCommandService;

  PostsController(
      PostApiInputFactory postApiInputFactory,
      PostApiMapper postApiMapper,
      PostQueryService postQueryService,
      PostCommandService postCommandService) {
    this.postApiInputFactory = postApiInputFactory;
    this.postApiMapper = postApiMapper;
    this.postQueryService = postQueryService;
    this.postCommandService = postCommandService;
  }

  @Override
  public ResponseEntity<List<PostSummary>> listPosts() {
    return ResponseEntity.ok(postApiMapper.toSummaries(postQueryService.list()));
  }

  @Override
  public ResponseEntity<PostResponse> createPost(
      String xCsrfToken, CreatePostRequest createPostRequest) {
    PostResponse response =
        postApiMapper.toResponse(
            postCommandService.create(postApiInputFactory.toCreateCommand(createPostRequest)));
    return ResponseEntity.created(
            UriComponentsBuilder.fromPath("/api/v1/posts/{slug}")
                .buildAndExpand(response.getSlug())
                .toUri())
        .body(response);
  }

  @Override
  public ResponseEntity<PostResponse> getPost(String slug) {
    return ResponseEntity.ok(postApiMapper.toResponse(postQueryService.get(slug)));
  }

  @Override
  public ResponseEntity<PostResponse> updatePost(
      String slug, String xCsrfToken, UpdatePostRequest updatePostRequest) {
    return ResponseEntity.ok(
        postApiMapper.toResponse(
            postCommandService.update(slug, postApiInputFactory.toPatch(updatePostRequest))));
  }

  @Override
  public ResponseEntity<PostResponse> publishPost(String slug, String xCsrfToken) {
    return ResponseEntity.ok(postApiMapper.toResponse(postCommandService.publish(slug)));
  }
}
