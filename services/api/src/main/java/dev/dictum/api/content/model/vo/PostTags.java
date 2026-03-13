package dev.dictum.api.content.model.vo;

import dev.dictum.api.content.error.InvalidPostRequestException;
import java.util.List;

public record PostTags(List<String> values) {

  private static final String TAGS_FIELD_NAME = "tags";

  public PostTags {
    if (values == null) {
      throw new InvalidPostRequestException("Field " + TAGS_FIELD_NAME + " cannot be null");
    }

    if (values.stream().anyMatch(tag -> tag == null || tag.isBlank())) {
      throw new InvalidPostRequestException(
          "Field " + TAGS_FIELD_NAME + " cannot contain blank values");
    }

    values = List.copyOf(values);
  }
}
