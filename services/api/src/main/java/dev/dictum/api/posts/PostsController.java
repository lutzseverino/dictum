package dev.dictum.api.posts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "Stub post contract endpoints")
public class PostsController {

  @GetMapping
  @Operation(summary = "List placeholder post entries")
  public PostsResponse listPosts() {
    return new PostsResponse(
        List.of(
            new PostSummaryResponse(
                "dictum-begins",
                "Dictum Begins",
                "published",
                "essay",
                "posts/dictum-begins/index.md",
                true),
            new PostSummaryResponse(
                "remote-controls-later",
                "Remote Controls, Later",
                "draft",
                "dispatch",
                "posts/remote-controls-later/index.md",
                false)));
  }

  public record PostsResponse(List<PostSummaryResponse> posts) {}
}
