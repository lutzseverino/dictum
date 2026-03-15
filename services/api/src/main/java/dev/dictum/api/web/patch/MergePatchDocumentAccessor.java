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
public class MergePatchDocumentAccessor {

  private static final String CACHED_BODY_ATTRIBUTE =
      MergePatchDocumentAccessor.class.getName() + ".currentDocument";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public MergePatchDocument currentDocument() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

    if (!(attributes.getRequest() instanceof ContentCachingRequestWrapper wrapper)) {
      throw new InvalidPatchRequestException("PATCH request body could not be inspected");
    }

    Object cachedDocument = wrapper.getAttribute(CACHED_BODY_ATTRIBUTE);
    if (cachedDocument instanceof MergePatchDocument mergePatchDocument) {
      return mergePatchDocument;
    }

    byte[] body = wrapper.getContentAsByteArray();
    JsonNode parsedBody = objectMapper.createObjectNode();

    if (body.length > 0) {
      try {
        // The generated merge-patch DTOs do not preserve field presence, so PATCH
        // semantics are derived from the raw request document instead.
        parsedBody = objectMapper.readTree(body);
      } catch (IOException exception) {
        throw new InvalidPatchRequestException("PATCH request body could not be parsed");
      }
    }

    MergePatchDocument document = new MergePatchDocument(parsedBody);
    wrapper.setAttribute(CACHED_BODY_ATTRIBUTE, document);
    return document;
  }
}
