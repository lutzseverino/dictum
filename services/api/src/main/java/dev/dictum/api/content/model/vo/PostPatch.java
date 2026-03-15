package dev.dictum.api.content.model.vo;

import dev.dictum.api.generated.model.UpdatePostRequest;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.PatchValue;
import java.util.List;

public record PostPatch(
    PatchValue<String> title,
    PatchValue<String> excerpt,
    PatchValue<dev.dictum.api.generated.model.PostTemplate> template,
    PatchValue<List<String>> tags,
    PatchValue<String> body,
    PatchValue<String> stylesheet,
    PatchValue<Boolean> removeStylesheet) {

  public static PostPatch from(UpdatePostRequest request, MergePatchDocument document) {
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
