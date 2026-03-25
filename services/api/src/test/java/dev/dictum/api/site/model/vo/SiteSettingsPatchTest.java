package dev.dictum.api.site.model.vo;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.PatchValue;
import org.junit.jupiter.api.Test;

class SiteSettingsPatchTest {

  @Test
  void validateRejectsExplicitNullSubtitle() {
    SiteSettingsPatch patch =
        new SiteSettingsPatch(
            PatchValue.absent(), PatchValue.explicitNullValue(), PatchValue.absent());

    assertThatThrownBy(patch::validate)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field subtitle cannot be null");
  }

  @Test
  void validateAllowsAbsentFields() {
    SiteSettingsPatch patch =
        new SiteSettingsPatch(PatchValue.absent(), PatchValue.absent(), PatchValue.absent());

    assertThatCode(patch::validate).doesNotThrowAnyException();
  }
}
