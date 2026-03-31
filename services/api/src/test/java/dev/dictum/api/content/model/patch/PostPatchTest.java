package dev.dictum.api.content.model.patch;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.PatchValue;
import org.junit.jupiter.api.Test;

class PostPatchTest {

  @Test
  void validateRejectsExplicitNullTags() {
    PostPatch patch =
        new PostPatch(
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.explicitNullValue(),
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.absent());

    assertThatThrownBy(patch::validate)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field tags cannot be null");
  }

  @Test
  void validateAllowsNullStylesheetWhenPresent() {
    PostPatch patch =
        new PostPatch(
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.absent(),
            PatchValue.explicitNullValue(),
            PatchValue.absent());

    assertThatCode(patch::validate).doesNotThrowAnyException();
  }
}
