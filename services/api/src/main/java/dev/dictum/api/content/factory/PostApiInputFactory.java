package dev.dictum.api.content.factory;

import dev.dictum.api.content.command.CreatePostCommand;
import dev.dictum.api.content.model.vo.PostPatch;
import dev.dictum.api.generated.model.CreatePostRequest;
import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
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
        request.getTemplate(),
        request.getTags(),
        request.getBody(),
        request.getStylesheet());
  }

  public PostPatch toPatch(UpdatePostRequest request) {
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();
    return new PostPatch(
        document.field("title", request.getTitle()),
        document.field("excerpt", request.getExcerpt()),
        document.field("template", request.getTemplate()),
        document.field("tags", request.getTags()),
        document.field("body", request.getBody()),
        document.field("stylesheet", request.getStylesheet()),
        document.field("removeStylesheet", request.getRemoveStylesheet()));
  }
}
