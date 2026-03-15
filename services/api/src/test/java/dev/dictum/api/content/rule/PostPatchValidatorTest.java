package dev.dictum.api.content.rule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.content.model.vo.PostPatch;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.PatchValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostPatchValidatorTest {

  private final PostPatchValidator postPatchValidator =
      new PostPatchValidator(List.of(new PostPatchRequiredValuesRule()));

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

    assertThatThrownBy(() -> postPatchValidator.validate(patch))
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

    assertThatCode(() -> postPatchValidator.validate(patch)).doesNotThrowAnyException();
  }
}
