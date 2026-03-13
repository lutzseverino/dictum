package dev.dictum.api.posts;

import java.util.List;
import java.util.regex.Pattern;

final class PostInputRules {

  private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

  private PostInputRules() {}

  static String requireValidSlug(String slug) {
    if (slug == null || !SLUG_PATTERN.matcher(slug).matches()) {
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
}
