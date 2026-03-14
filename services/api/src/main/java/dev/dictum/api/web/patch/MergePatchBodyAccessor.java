package dev.dictum.api.web.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class MergePatchBodyAccessor {

  private static final String CACHED_BODY_ATTRIBUTE =
      MergePatchBodyAccessor.class.getName() + ".currentBody";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public boolean containsField(String fieldName) {
    return currentBody().has(fieldName);
  }

  public boolean isExplicitNull(String fieldName) {
    return currentBody().has(fieldName) && currentBody().get(fieldName).isNull();
  }

  public void requireAnyField() {
    JsonNode body = currentBody();

    if (!body.isObject()) {
      throw new InvalidPatchRequestException("PATCH requests must use a JSON object body");
    }

    if (body.isEmpty()) {
      throw new InvalidPatchRequestException("PATCH requests must include at least one field");
    }
  }

  private JsonNode currentBody() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

    if (!(attributes.getRequest() instanceof ContentCachingRequestWrapper wrapper)) {
      throw new InvalidPatchRequestException("PATCH request body could not be inspected");
    }

    Object cachedBody = wrapper.getAttribute(CACHED_BODY_ATTRIBUTE);
    if (cachedBody instanceof JsonNode jsonNode) {
      return jsonNode;
    }

    byte[] body = wrapper.getContentAsByteArray();
    JsonNode parsedBody;

    if (body.length == 0) {
      parsedBody = objectMapper.createObjectNode();
      wrapper.setAttribute(CACHED_BODY_ATTRIBUTE, parsedBody);
      return parsedBody;
    }

    try {
      // The generated merge-patch DTOs do not preserve field presence, so PATCH
      // semantics are derived from the raw request document instead.
      parsedBody = objectMapper.readTree(body);
      wrapper.setAttribute(CACHED_BODY_ATTRIBUTE, parsedBody);
      return parsedBody;
    } catch (IOException exception) {
      throw new InvalidPatchRequestException("PATCH request body could not be parsed");
    }
  }
}
