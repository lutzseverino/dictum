package dev.dictum.api.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.mapper.PostApiMapperImpl;
import dev.dictum.api.content.repository.InMemoryPostStore;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchBodyAccessor;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

  @Mock private MergePatchBodyAccessor mergePatchBodyAccessor;

  private InMemoryPostStore postStore;
  private PostCommandService postCommandService;

  @BeforeEach
  void setUp() {
    postStore = newPostStore();
    postCommandService =
        new PostCommandService(postStore, new PostApiMapperImpl(), mergePatchBodyAccessor);
  }

  @Test
  void createPersistsADraftPostWithDerivedPaths() {
    CreatePostRequest request =
        new CreatePostRequest(
                "Notes on Remote Editing",
                "notes-on-remote-editing",
                "First thoughts on a phone-first publishing workflow.",
                PostTemplate.NOTE,
                List.of("product", "mobile"),
                "Remote editing should stay lightweight.")
            .stylesheet("body { color: tomato; }");

    PostResponse response = postCommandService.create(request);

    assertThat(response.getSlug()).isEqualTo("notes-on-remote-editing");
    assertThat(response.getStatus()).isEqualTo(PostStatus.DRAFT);
    assertThat(response.getHasStylesheet()).isTrue();
    assertThat(response.getContentPath()).isEqualTo("posts/notes-on-remote-editing/index.md");
    assertThat(response.getStylesheetPath()).isEqualTo("posts/notes-on-remote-editing/style.css");
    assertThat(postStore.findBySlug("notes-on-remote-editing")).isPresent();
  }

  @Test
  void createRejectsDuplicateSlugs() {
    CreatePostRequest request =
        new CreatePostRequest(
            "Duplicate",
            "dictum-begins",
            "Already exists.",
            PostTemplate.ESSAY,
            List.of("architecture"),
            "Duplicate body.");

    assertThatThrownBy(() -> postCommandService.create(request))
        .isInstanceOf(PostConflictException.class)
        .hasMessage("A post already exists for slug dictum-begins");
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    when(mergePatchBodyAccessor.containsField("title")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("excerpt")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("template")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("tags")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("body")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("stylesheet")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("removeStylesheet")).thenReturn(false);
    when(mergePatchBodyAccessor.isExplicitNull("tags")).thenReturn(false);

    UpdatePostRequest request =
        new UpdatePostRequest().title("Remote Controls, Sooner").tags(List.of("admin", "api"));

    PostResponse response = postCommandService.update("remote-controls-later", request);

    assertThat(response.getTitle()).isEqualTo("Remote Controls, Sooner");
    assertThat(response.getExcerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(response.getTags()).containsExactly("admin", "api");
    verify(mergePatchBodyAccessor).isExplicitNull("tags");
  }

  @Test
  void updateRemovesTheStylesheetWhenTheFieldIsPatchedToNull() {
    when(mergePatchBodyAccessor.containsField("title")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("excerpt")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("template")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("tags")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("body")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("stylesheet")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("removeStylesheet")).thenReturn(false);

    PostResponse response =
        postCommandService.update("dictum-begins", new UpdatePostRequest().stylesheet(null));

    assertThat(response.getHasStylesheet()).isFalse();
    assertThat(response.getStylesheetPath()).isNull();
  }

  @Test
  void updateRejectsExplicitNullTags() {
    when(mergePatchBodyAccessor.containsField("title")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("excerpt")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("template")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("tags")).thenReturn(true);
    when(mergePatchBodyAccessor.containsField("body")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("stylesheet")).thenReturn(false);
    when(mergePatchBodyAccessor.containsField("removeStylesheet")).thenReturn(false);
    when(mergePatchBodyAccessor.isExplicitNull("tags")).thenReturn(true);

    assertThatThrownBy(
            () ->
                postCommandService.update(
                    "remote-controls-later", new UpdatePostRequest().tags(List.of("admin"))))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field tags cannot be null");
  }

  @Test
  void publishTransitionsTheDraftToPublished() {
    PostResponse response = postCommandService.publish("remote-controls-later");

    assertThat(response.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    assertThat(response.getPublishedAt()).isNotNull();
  }

  @Test
  void publishRejectsAlreadyPublishedPosts() {
    assertThatThrownBy(() -> postCommandService.publish("dictum-begins"))
        .isInstanceOf(PostConflictException.class)
        .hasMessage("Post dictum-begins is already published");
  }

  @Test
  void updateRejectsUnknownSlugs() {
    assertThatThrownBy(
            () -> postCommandService.update("unknown-slug", new UpdatePostRequest().title("Nope")))
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
