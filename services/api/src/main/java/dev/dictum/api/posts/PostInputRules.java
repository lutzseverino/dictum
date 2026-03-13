package dev.dictum.api.posts;

import java.util.List;

final class PostInputRules {

  private PostInputRules() {}

  static String requireValidSlug(String slug) {
    if (!isValidSlug(slug)) {
      throw new InvalidPostRequestException(
          "Slug must be lowercase kebab-case using letters, numbers, and hyphens");
    }

    return slug;
  }

  static List<String> copyTags(String fieldName, List<String> tags) {
    if (tags == null) {
      throw new InvalidPostRequestException("Field " + fieldName + " cannot be null");
    }

    if (tags.stream().anyMatch(tag -> tag == null || tag.isBlank())) {
      throw new InvalidPostRequestException("Field " + fieldName + " cannot contain blank values");
    }

    return List.copyOf(tags);
  }

  private static boolean isValidSlug(String slug) {
    if (slug == null || slug.isBlank()) {
      return false;
    }

    if (slug.charAt(0) == '-' || slug.charAt(slug.length() - 1) == '-') {
      return false;
    }

    boolean previousWasHyphen = false;

    for (int index = 0; index < slug.length(); index++) {
      char current = slug.charAt(index);

      if (current == '-') {
        if (previousWasHyphen) {
          return false;
        }

        previousWasHyphen = true;
        continue;
      }

      if (!Character.isDigit(current) && (current < 'a' || current > 'z')) {
        return false;
      }

      previousWasHyphen = false;
    }

    return true;
  }
}
