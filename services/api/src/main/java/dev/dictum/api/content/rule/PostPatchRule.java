package dev.dictum.api.content.rule;

import dev.dictum.api.content.model.vo.PostPatch;

public interface PostPatchRule {

  void validate(PostPatch patch);
}
