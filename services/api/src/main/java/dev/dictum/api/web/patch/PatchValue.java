package dev.dictum.api.web.patch;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import org.jspecify.annotations.Nullable;

public record PatchValue<T>(boolean present, boolean explicitNull, @Nullable T value) {

  public static <T> PatchValue<T> absent() {
    return new PatchValue<>(false, false, null);
  }

  public static <T> PatchValue<T> present(@Nullable T value) {
    return new PatchValue<>(true, false, value);
  }

  public static <T> PatchValue<T> explicitNullValue() {
    return new PatchValue<>(true, true, null);
  }

  public boolean isPresent() {
    return present;
  }

  public boolean isExplicitNull() {
    return explicitNull;
  }

  public void requireNonNullWhenPresent(String fieldName) {
    if (present && value == null) {
      throw new InvalidPatchRequestException("Field " + fieldName + " cannot be null");
    }
  }
}
