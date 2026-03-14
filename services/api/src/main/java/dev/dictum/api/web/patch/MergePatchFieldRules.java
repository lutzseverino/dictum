package dev.dictum.api.web.patch;

import dev.dictum.api.web.error.InvalidPatchRequestException;

public final class MergePatchFieldRules {

  private MergePatchFieldRules() {}

  public static void requireNonNullWhenPresent(
      String fieldName, boolean fieldPresent, Object value) {
    if (fieldPresent && value == null) {
      throw new InvalidPatchRequestException("Field " + fieldName + " cannot be null");
    }
  }
}
