package dev.dictum.api.content.rule;

import dev.dictum.api.content.model.vo.PostPatch;
import org.springframework.stereotype.Component;

@Component
public class PostPatchRequiredValuesRule implements PostPatchRule {

  @Override
  public void validate(PostPatch patch) {
    patch.title().requireNonNullWhenPresent("title");
    patch.excerpt().requireNonNullWhenPresent("excerpt");
    patch.template().requireNonNullWhenPresent("template");
    patch.tags().requireNonNullWhenPresent("tags");
    patch.body().requireNonNullWhenPresent("body");
    patch.removeStylesheet().requireNonNullWhenPresent("removeStylesheet");
  }
}
