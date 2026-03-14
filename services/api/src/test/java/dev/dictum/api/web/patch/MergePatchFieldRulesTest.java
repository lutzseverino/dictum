package dev.dictum.api.web.patch;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import org.junit.jupiter.api.Test;

class MergePatchFieldRulesTest {

  @Test
  void requireNonNullWhenPresentAllowsAbsentFields() {
    assertThatCode(() -> MergePatchFieldRules.requireNonNullWhenPresent("title", false, null))
        .doesNotThrowAnyException();
  }

  @Test
  void requireNonNullWhenPresentRejectsExplicitNullValues() {
    assertThatThrownBy(() -> MergePatchFieldRules.requireNonNullWhenPresent("title", true, null))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field title cannot be null");
  }
}
