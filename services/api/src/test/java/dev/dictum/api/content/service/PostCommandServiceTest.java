package dev.dictum.api.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.content.command.CreatePostCommand;
import dev.dictum.api.content.error.InvalidPostRequestException;
import dev.dictum.api.content.error.PostConflictException;
import dev.dictum.api.content.error.PostNotFoundException;
import dev.dictum.api.content.model.patch.PostPatch;
import dev.dictum.api.content.model.state.PostState;
import dev.dictum.api.content.model.vo.PostStatus;
import dev.dictum.api.content.model.vo.PostTemplate;
import dev.dictum.api.content.store.InMemoryPostStore;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.MergePatchDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostCommandServiceTest {

  private static final String DICTUM_BEGINS_SLUG = "dictum-begins";
  private static final String REMOTE_CONTROLS_LATER_SLUG = "remote-controls-later";
  private static final String NOTES_ON_REMOTE_EDITING_SLUG = "notes-on-remote-editing";
  private static final String ADMIN_TAG = "admin";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private InMemoryPostStore postStore;
  private PostCommandService postCommandService;

  @BeforeEach
  void setUp() {
    postStore = new InMemoryPostStore();
    postCommandService = new PostCommandService(postStore);
  }

  @Test
  void createPersistsADraftPost() {
    CreatePostCommand command =
        new CreatePostCommand(
            "Notes on Remote Editing",
            NOTES_ON_REMOTE_EDITING_SLUG,
            "First thoughts on a phone-first publishing workflow.",
            PostTemplate.NOTE,
            List.of("product", "mobile"),
            "Remote editing should stay lightweight.",
            "body { color: tomato; }");

    PostState created = postCommandService.create(command);

    assertThat(created.slug()).isEqualTo(NOTES_ON_REMOTE_EDITING_SLUG);
    assertThat(created.status()).isEqualTo(PostStatus.DRAFT);
    assertThat(created.hasStylesheet()).isTrue();
    assertThat(created.stylesheetContent()).isEqualTo("body { color: tomato; }");
    assertThat(postStore.findBySlug(NOTES_ON_REMOTE_EDITING_SLUG)).isPresent();
  }

  @Test
  void createRejectsDuplicateSlugs() {
    CreatePostCommand command =
        new CreatePostCommand(
            "Duplicate",
            DICTUM_BEGINS_SLUG,
            "Already exists.",
            PostTemplate.ESSAY,
            List.of("architecture"),
            "Duplicate body.",
            null);

    assertThatThrownBy(() -> postCommandService.create(command))
        .isInstanceOf(PostConflictException.class)
        .hasMessage("A post already exists for slug " + DICTUM_BEGINS_SLUG);
  }

  @Test
  void createRejectsTagListsContainingNullEntries() {
    List<String> tags = new ArrayList<>(List.of("news"));
    tags.add(null);

    CreatePostCommand command =
        new CreatePostCommand(
            "Bad tags", "bad-tags", "Contains a null tag.", PostTemplate.NOTE, tags, "Body.", null);

    assertThatThrownBy(() -> postCommandService.create(command))
        .isInstanceOf(InvalidPostRequestException.class)
        .hasMessage("Field tags cannot contain blank values");
  }

  @Test
  void updateChangesOnlyTheProvidedFields() {
    PostPatch patch =
        patch(
            "{\"title\":\"Remote Controls, Sooner\",\"tags\":[\"admin\",\"api\"]}",
            "Remote Controls, Sooner",
            List.of(ADMIN_TAG, "api"));

    PostState updated = postCommandService.update(REMOTE_CONTROLS_LATER_SLUG, patch);

    assertThat(updated.title()).isEqualTo("Remote Controls, Sooner");
    assertThat(updated.excerpt())
        .isEqualTo("The admin experience will later own publish and settings mutations.");
    assertThat(updated.tags()).containsExactly(ADMIN_TAG, "api");
  }

  @Test
  void updateRemovesTheStylesheetWhenTheFieldIsPatchedToNull() {
    PostState updated =
        postCommandService.update(DICTUM_BEGINS_SLUG, patch("{\"stylesheet\":null}", null, null));

    assertThat(updated.hasStylesheet()).isFalse();
    assertThat(updated.stylesheetContent()).isNull();
  }

  @Test
  void updateRejectsExplicitNullTags() {
    assertThatThrownBy(
            () ->
                postCommandService.update(
                    REMOTE_CONTROLS_LATER_SLUG, patch("{\"tags\":null}", null, null)))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field tags cannot be null");
  }

  @Test
  void publishTransitionsTheDraftToPublished() {
    PostState published = postCommandService.publish(REMOTE_CONTROLS_LATER_SLUG);

    assertThat(published.status()).isEqualTo(PostStatus.PUBLISHED);
    assertThat(published.publishedAt()).isNotNull();
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
            () ->
                postCommandService.update(
                    "unknown-slug", patch("{\"title\":\"Nope\"}", "Nope", null)))
        .isInstanceOf(PostNotFoundException.class)
        .hasMessage("No post exists for slug unknown-slug");
  }

  private PostPatch patch(String json, String title, List<String> tags) {
    dev.dictum.api.generated.model.UpdatePostRequest request =
        new dev.dictum.api.generated.model.UpdatePostRequest().title(title).tags(tags);
    MergePatchDocument document = patchDocument(json);

    return new PostPatch(
        document.field("title", request.getTitle()),
        document.field("excerpt", request.getExcerpt()),
        toTemplatePatch(document.field("template", request.getTemplate())),
        document.field("tags", request.getTags()),
        document.field("body", request.getBody()),
        document.field("stylesheet", request.getStylesheet()),
        document.field("removeStylesheet", request.getRemoveStylesheet()));
  }

  private dev.dictum.api.web.patch.PatchValue<PostTemplate> toTemplatePatch(
      dev.dictum.api.web.patch.PatchValue<dev.dictum.api.generated.model.PostTemplate>
          templatePatch) {
    if (!templatePatch.isPresent()) {
      return dev.dictum.api.web.patch.PatchValue.absent();
    }

    if (templatePatch.isExplicitNull()) {
      return dev.dictum.api.web.patch.PatchValue.explicitNullValue();
    }

    return dev.dictum.api.web.patch.PatchValue.present(
        PostTemplate.fromValue(templatePatch.value().getValue()));
  }

  private MergePatchDocument patchDocument(String json) {
    try {
      return new MergePatchDocument(OBJECT_MAPPER.readTree(json));
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to create merge patch document for tests", exception);
    }
  }
}
