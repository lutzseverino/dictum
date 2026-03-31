package dev.dictum.api.content.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostStatus {
  DRAFT("draft"),
  PUBLISHED("published");

  private final String value;

  PostStatus(String value) {
    this.value = value;
  }

  @JsonCreator
  public static PostStatus fromValue(String value) {
    for (PostStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }

    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  @JsonValue
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
