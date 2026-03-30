package dev.dictum.api.content.factory;

import dev.dictum.api.content.command.CreatePostCommand;
import dev.dictum.api.content.model.patch.PostPatch;
import dev.dictum.api.content.model.vo.PostTemplate;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import dev.dictum.api.web.patch.PatchValue;
import org.springframework.stereotype.Component;

@Component
public class PostApiInputFactory {

  private final MergePatchDocumentAccessor mergePatchDocumentAccessor;

  PostApiInputFactory(MergePatchDocumentAccessor mergePatchDocumentAccessor) {
    this.mergePatchDocumentAccessor = mergePatchDocumentAccessor;
  }

  public CreatePostCommand toCreateCommand(CreatePostRequest request) {
    return new CreatePostCommand(
        request.getTitle(),
        request.getSlug(),
        request.getExcerpt(),
        PostTemplate.fromValue(request.getTemplate().getValue()),
        request.getTags(),
        request.getBody(),
        request.getStylesheet());
  }

  public PostPatch toPatch(UpdatePostRequest request) {
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();
    return new PostPatch(
        document.field("title", request.getTitle()),
        document.field("excerpt", request.getExcerpt()),
        toTemplatePatch(document.field("template", request.getTemplate())),
        document.field("tags", request.getTags()),
        document.field("body", request.getBody()),
        document.field("stylesheet", request.getStylesheet()),
        document.field("removeStylesheet", request.getRemoveStylesheet()));
  }

  private PatchValue<PostTemplate> toTemplatePatch(
      PatchValue<dev.dictum.api.generated.model.PostTemplate> templatePatch) {
    if (!templatePatch.isPresent()) {
      return PatchValue.absent();
    }

    if (templatePatch.isExplicitNull()) {
      return PatchValue.explicitNullValue();
    }

    return PatchValue.present(PostTemplate.fromValue(templatePatch.value().getValue()));
  }
}
