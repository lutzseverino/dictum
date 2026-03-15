package dev.dictum.api.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.mapper.PostApiMapperImpl;
import dev.dictum.api.content.repository.InMemoryPostRepository;
import dev.dictum.api.content.rule.PostPatchRequiredValuesRule;
import dev.dictum.api.content.rule.PostPatchValidator;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import java.io.IOException;
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
  private static final String ADMIN_TAG = "admin";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mock private MergePatchDocumentAccessor mergePatchDocumentAccessor;

  private InMemoryPostRepository postRepository;
  private PostCommandService postCommandService;

  @BeforeEach
  void setUp() {
    postRepository = new InMemoryPostRepository();
    postCommandService =
        new PostCommandService(
            postRepository,
            new PostApiMapperImpl(),
            new PostPatchValidator(List.of(new PostPatchRequiredValuesRule())),
            mergePatchDocumentAccessor);
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
    assertThat(postRepository.findBySlug(NOTES_ON_REMOTE_EDITING_SLUG)).isPresent();
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
    when(mergePatchDocumentAccessor.currentDocument())
        .thenReturn(
            patchDocument("{\"title\":\"Remote Controls, Sooner\",\"tags\":[\"admin\",\"api\"]}"));

    UpdatePostRequest request =
        new UpdatePostRequest().title("Remote Controls, Sooner").tags(List.of(ADMIN_TAG, "api"));

    PostResponse response = postCommandService.update(REMOTE_CONTROLS_LATER_SLUG, request);

    assertThat(response.getTitle()).isEqualTo("Remote Controls, Sooner");
    assertThat(response.getExcerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(response.getTags()).containsExactly(ADMIN_TAG, "api");
  }

  @Test
  void updateRemovesTheStylesheetWhenTheFieldIsPatchedToNull() {
    when(mergePatchDocumentAccessor.currentDocument())
        .thenReturn(patchDocument("{\"stylesheet\":null}"));

    PostResponse response =
        postCommandService.update(DICTUM_BEGINS_SLUG, new UpdatePostRequest().stylesheet(null));

    assertThat(response.getHasStylesheet()).isFalse();
    assertThat(response.getStylesheetPath()).isNull();
  }

  @Test
  void updateRejectsExplicitNullTags() {
    when(mergePatchDocumentAccessor.currentDocument()).thenReturn(patchDocument("{\"tags\":null}"));

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

  private MergePatchDocument patchDocument(String json) {
    try {
      return new MergePatchDocument(OBJECT_MAPPER.readTree(json));
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to create merge patch document for tests", exception);
    }
  }
}
