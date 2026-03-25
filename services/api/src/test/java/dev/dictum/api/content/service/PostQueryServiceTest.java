package dev.dictum.api.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.content.store.InMemoryPostStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostQueryServiceTest {

  private static final String DICTUM_BEGINS_SLUG = "dictum-begins";

  private PostQueryService postQueryService;

  @BeforeEach
  void setUp() {
    postQueryService = new PostQueryService(new InMemoryPostStore());
  }

  @Test
  void listReturnsTheSeededPostStates() {
    List<PostState> responses = postQueryService.list();

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).slug()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(responses.get(0).status().getValue()).isEqualTo("published");
    assertThat(responses.get(1).slug()).isEqualTo("remote-controls-later");
  }

  @Test
  void getReturnsTheSeededPostDetail() {
    PostState response = postQueryService.get(DICTUM_BEGINS_SLUG);

    assertThat(response.slug()).isEqualTo(DICTUM_BEGINS_SLUG);
    assertThat(response.hasStylesheet()).isTrue();
    assertThat(response.stylesheetContent()).contains("border-inline-start");
  }

  @Test
  void getRejectsUnknownSlugs() {
    assertThatThrownBy(() -> postQueryService.get("unknown-slug"))
        .isInstanceOf(PostNotFoundException.class)
        .hasMessage("No post exists for slug unknown-slug");
  }
}
