package dev.dictum.api.content.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostTemplate {
  ESSAY("essay"),
  NOTE("note"),
  DISPATCH("dispatch");

  private final String value;

  PostTemplate(String value) {
    this.value = value;
  }

  @JsonCreator
  public static PostTemplate fromValue(String value) {
    for (PostTemplate template : values()) {
      if (template.value.equals(value)) {
        return template;
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
