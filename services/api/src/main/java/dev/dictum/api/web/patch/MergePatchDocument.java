package dev.dictum.api.web.patch;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import org.springframework.lang.Nullable;

public final class MergePatchDocument {

  private final JsonNode body;

  public MergePatchDocument(JsonNode body) {
    if (!body.isObject()) {
      throw new InvalidPatchRequestException("PATCH requests must use a JSON object body");
    }

    if (body.isEmpty()) {
      throw new InvalidPatchRequestException("PATCH requests must include at least one field");
    }

    this.body = body;
  }

  public <T> PatchValue<T> field(String fieldName, @Nullable T value) {
    if (!body.has(fieldName)) {
      return PatchValue.absent();
    }

    if (body.get(fieldName).isNull()) {
      return PatchValue.explicitNullValue();
    }

    return PatchValue.present(value);
  }
}
