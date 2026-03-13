package dev.dictum.api.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.settings.InvalidPatchRequestException;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class MergePatchBodyAccessor {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public boolean containsField(String fieldName) {
    return currentBody().has(fieldName);
  }

  public boolean isExplicitNull(String fieldName) {
    return currentBody().has(fieldName) && currentBody().get(fieldName).isNull();
  }

  public void requireAnyField() {
    if (currentBody().isEmpty()) {
      throw new InvalidPatchRequestException("PATCH requests must include at least one field");
    }
  }

  private JsonNode currentBody() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

    if (!(attributes.getRequest() instanceof ContentCachingRequestWrapper wrapper)) {
      throw new InvalidPatchRequestException("PATCH request body could not be inspected");
    }

    byte[] body = wrapper.getContentAsByteArray();

    if (body.length == 0) {
      return objectMapper.createObjectNode();
    }

    try {
      // The generated merge-patch DTOs do not preserve field presence, so PATCH
      // semantics are derived from the raw request document instead.
      return objectMapper.readTree(body);
    } catch (IOException exception) {
      throw new InvalidPatchRequestException("PATCH request body could not be parsed");
    }
  }
}
