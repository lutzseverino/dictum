package dev.dictum.api.site.rule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import dev.dictum.api.web.patch.PatchValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class SiteSettingsPatchValidatorTest {

  private final SiteSettingsPatchValidator siteSettingsPatchValidator =
      new SiteSettingsPatchValidator(List.of(new SiteSettingsPatchRequiredValuesRule()));

  @Test
  void validateRejectsExplicitNullSubtitle() {
    SiteSettingsPatch patch =
        new SiteSettingsPatch(
            PatchValue.absent(), PatchValue.explicitNullValue(), PatchValue.absent());

    assertThatThrownBy(() -> siteSettingsPatchValidator.validate(patch))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field subtitle cannot be null");
  }

  @Test
  void validateAllowsAbsentFields() {
    SiteSettingsPatch patch =
        new SiteSettingsPatch(PatchValue.absent(), PatchValue.absent(), PatchValue.absent());

    assertThatCode(() -> siteSettingsPatchValidator.validate(patch)).doesNotThrowAnyException();
  }
}
