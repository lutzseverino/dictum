package dev.dictum.api.content.model.patch;

import dev.dictum.api.content.model.state.PostState;
import dev.dictum.api.content.model.vo.PostTags;
import dev.dictum.api.content.model.vo.PostTemplate;
import dev.dictum.api.web.patch.PatchValue;
import java.util.List;

public record PostPatch(
    PatchValue<String> title,
    PatchValue<String> excerpt,
    PatchValue<PostTemplate> template,
    PatchValue<List<String>> tags,
    PatchValue<String> body,
    PatchValue<String> stylesheet,
    PatchValue<Boolean> removeStylesheet) {

  public void validate() {
    title().requireNonNullWhenPresent("title");
    excerpt().requireNonNullWhenPresent("excerpt");
    template().requireNonNullWhenPresent("template");
    tags().requireNonNullWhenPresent("tags");
    body().requireNonNullWhenPresent("body");
    removeStylesheet().requireNonNullWhenPresent("removeStylesheet");
  }

  public PostState applyTo(PostState current) {
    String stylesheetContent = resolveStylesheetContent(current.stylesheetContent());

    return new PostState(
        current.slug(),
        title().isPresent() ? title().value() : current.title(),
        excerpt().isPresent() ? excerpt().value() : current.excerpt(),
        current.status(),
        template().isPresent() ? template().value() : current.template(),
        current.publishedAt(),
        tags().isPresent() ? new PostTags(tags().value()).values() : current.tags(),
        stylesheetContent != null,
        body().isPresent() ? body().value() : current.body(),
        stylesheetContent);
  }

  private String resolveStylesheetContent(String currentStylesheetContent) {
    if (Boolean.TRUE.equals(removeStylesheet().value())) {
      return null;
    }

    if (stylesheet().isPresent()) {
      return stylesheet().value();
    }

    return currentStylesheetContent;
  }
}
