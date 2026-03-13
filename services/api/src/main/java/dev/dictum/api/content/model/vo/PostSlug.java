package dev.dictum.api.content.model.vo;

import dev.dictum.api.content.error.InvalidPostRequestException;

public record PostSlug(String value) {

  private static final String INVALID_SLUG_MESSAGE =
      "Slug must be lowercase kebab-case using letters, numbers, and hyphens";

  public PostSlug {
    if (value == null || value.isBlank()) {
      throw new InvalidPostRequestException(INVALID_SLUG_MESSAGE);
    }

    if (value.charAt(0) == '-' || value.charAt(value.length() - 1) == '-') {
      throw new InvalidPostRequestException(INVALID_SLUG_MESSAGE);
    }

    boolean previousWasHyphen = false;

    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);

      if (current == '-') {
        if (previousWasHyphen) {
          throw new InvalidPostRequestException(INVALID_SLUG_MESSAGE);
        }

        previousWasHyphen = true;
        continue;
      }

      if (!Character.isDigit(current) && (current < 'a' || current > 'z')) {
        throw new InvalidPostRequestException(INVALID_SLUG_MESSAGE);
      }

      previousWasHyphen = false;
    }
  }
}
