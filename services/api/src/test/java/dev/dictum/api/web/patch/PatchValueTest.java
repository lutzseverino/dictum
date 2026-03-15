package dev.dictum.api.web.patch;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import org.junit.jupiter.api.Test;

class PatchValueTest {

  @Test
  void requireNonNullWhenPresentAllowsAbsentFields() {
    assertThatCode(() -> PatchValue.absent().requireNonNullWhenPresent("title"))
        .doesNotThrowAnyException();
  }

  @Test
  void requireNonNullWhenPresentRejectsExplicitNullValues() {
    assertThatThrownBy(() -> PatchValue.explicitNullValue().requireNonNullWhenPresent("title"))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("Field title cannot be null");
  }
}
