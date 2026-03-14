package dev.dictum.api.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.mapper.PostApiMapperImpl;
import dev.dictum.api.content.repository.InMemoryPostStore;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostQueryServiceTest {

  private PostQueryService postQueryService;

  @BeforeEach
  void setUp() {
    postQueryService = new PostQueryService(newPostStore(), new PostApiMapperImpl());
  }

  @Test
  void listResponsesReturnsTheSeededPostSummaries() {
    List<PostSummary> responses = postQueryService.listResponses();

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).getSlug()).isEqualTo("dictum-begins");
    assertThat(responses.get(0).getStatus().getValue()).isEqualTo("published");
    assertThat(responses.get(1).getSlug()).isEqualTo("remote-controls-later");
  }

  @Test
  void getResponseReturnsTheSeededPostDetail() {
    PostResponse response = postQueryService.getResponse("dictum-begins");

    assertThat(response.getSlug()).isEqualTo("dictum-begins");
    assertThat(response.getContentPath()).isEqualTo("posts/dictum-begins/index.md");
    assertThat(response.getStylesheetPath()).isEqualTo("posts/dictum-begins/style.css");
  }

  @Test
  void getResponseRejectsUnknownSlugs() {
    assertThatThrownBy(() -> postQueryService.getResponse("unknown-slug"))
        .isInstanceOf(PostNotFoundException.class)
        .hasMessage("No post exists for slug unknown-slug");
  }

  private InMemoryPostStore newPostStore() {
    try {
      Constructor<InMemoryPostStore> constructor = InMemoryPostStore.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to create in-memory post store for tests", exception);
    }
  }
}
