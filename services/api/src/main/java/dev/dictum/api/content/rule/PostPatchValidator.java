package dev.dictum.api.content.rule;

import dev.dictum.api.content.model.vo.PostPatch;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostPatchValidator {

  private final List<PostPatchRule> rules;

  public PostPatchValidator(List<PostPatchRule> rules) {
    this.rules = List.copyOf(rules);
  }

  public void validate(PostPatch patch) {
    rules.forEach(rule -> rule.validate(patch));
  }
}
