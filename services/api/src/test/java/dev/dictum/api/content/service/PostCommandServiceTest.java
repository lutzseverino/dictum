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

  private static final String DICTUM_BEGINS_SLUG = "dictum-begins";
  private static final String REMOTE_CONTROLS_LATER_SLUG = "remote-controls-later";
  private static final String NOTES_ON_REMOTE_EDITING_SLUG = "notes-on-remote-editing";
  private static final String TITLE_FIELD = "title";
  private static final String EXCERPT_FIELD = "excerpt";
  private static final String TEMPLATE_FIELD = "template";
  private static final String TAGS_FIELD = "tags";
  private static final String BODY_FIELD = "body";
  private static final String STYLESHEET_FIELD = "stylesheet";
  private static final String REMOVE_STYLESHEET_FIELD = "removeStylesheet";
  private static final String ADMIN_TAG = "admin";

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
                NOTES_ON_REMOTE_EDITING_SLUG,
                "First thoughts on a phone-first publishing workflow.",
                PostTemplate.NOTE,
                List.of("product", "mobile"),
                "Remote editing should stay lightweight.")
            .stylesheet("body { color: tomato; }");

    PostResponse response = postCommandService.create(request);

    assertThat(response.getSlug()).isEqualTo(NOTES_ON_REMOTE_EDITING_SLUG);
    assertThat(response.getStatus()).isEqualTo(PostStatus.DRAFT);
    assertThat(response.getHasStylesheet()).isTrue();
    assertThat(response.getContentPath())
        .isEqualTo("posts/" + NOTES_ON_REMOTE_EDITING_SLUG + "/index.md");
    assertThat(response.getStylesheetPath())
        .isEqualTo("posts/" + NOTES_ON_REMOTE_EDITING_SLUG + "/style.css");
    assertThat(postStore.findBySlug(NOTES_ON_REMOTE_EDITING_SLUG)).isPresent();
  }

  @Test
  void createRejectsDuplicateSlugs() {
    CreatePostRequest request =
        new CreatePostRequest(
            "Duplicate",
            DICTUM_BEGINS_SLUG,
            "Already exists.",
            PostTemplate.ESSAY,
            List.of("architecture"),
            "Duplicate body.");

    assertThatThrownBy(() -> postCommandService.create(request))
        .isInstanceOf(PostConflictException.class)
        .hasMessage("A post already exists for slug " + DICTUM_BEGINS_SLUG);
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    when(mergePatchBodyAccessor.containsField(TITLE_FIELD)).thenReturn(true);
    when(mergePatchBodyAccessor.containsField(EXCERPT_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TEMPLATE_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TAGS_FIELD)).thenReturn(true);
    when(mergePatchBodyAccessor.containsField(BODY_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(STYLESHEET_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(REMOVE_STYLESHEET_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.isExplicitNull(TAGS_FIELD)).thenReturn(false);

    UpdatePostRequest request =
        new UpdatePostRequest().title("Remote Controls, Sooner").tags(List.of(ADMIN_TAG, "api"));

    PostResponse response = postCommandService.update(REMOTE_CONTROLS_LATER_SLUG, request);

    assertThat(response.getTitle()).isEqualTo("Remote Controls, Sooner");
    assertThat(response.getExcerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(response.getTags()).containsExactly(ADMIN_TAG, "api");
    verify(mergePatchBodyAccessor).isExplicitNull(TAGS_FIELD);
  }

  @Test
  void updateRemovesTheStylesheetWhenTheFieldIsPatchedToNull() {
    when(mergePatchBodyAccessor.containsField(TITLE_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(EXCERPT_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TEMPLATE_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TAGS_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(BODY_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(STYLESHEET_FIELD)).thenReturn(true);
    when(mergePatchBodyAccessor.containsField(REMOVE_STYLESHEET_FIELD)).thenReturn(false);

    PostResponse response =
        postCommandService.update(DICTUM_BEGINS_SLUG, new UpdatePostRequest().stylesheet(null));

    assertThat(response.getHasStylesheet()).isFalse();
    assertThat(response.getStylesheetPath()).isNull();
  }

  @Test
  void updateRejectsExplicitNullTags() {
    when(mergePatchBodyAccessor.containsField(TITLE_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(EXCERPT_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TEMPLATE_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(TAGS_FIELD)).thenReturn(true);
    when(mergePatchBodyAccessor.containsField(BODY_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(STYLESHEET_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.containsField(REMOVE_STYLESHEET_FIELD)).thenReturn(false);
    when(mergePatchBodyAccessor.isExplicitNull(TAGS_FIELD)).thenReturn(true);

    assertThatThrownBy(
            () ->
                postCommandService.update(
                    REMOTE_CONTROLS_LATER_SLUG, new UpdatePostRequest().tags(List.of(ADMIN_TAG))))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field tags cannot be null");
  }

  @Test
  void publishTransitionsTheDraftToPublished() {
    PostResponse response = postCommandService.publish(REMOTE_CONTROLS_LATER_SLUG);

    assertThat(response.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    assertThat(response.getPublishedAt()).isNotNull();
  }

  @Test
  void publishRejectsAlreadyPublishedPosts() {
    assertThatThrownBy(() -> postCommandService.publish(DICTUM_BEGINS_SLUG))
        .isInstanceOf(PostConflictException.class)
        .hasMessage("Post " + DICTUM_BEGINS_SLUG + " is already published");
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
