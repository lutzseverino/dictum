package dev.dictum.api.web.patch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.dictum.api.web.error.InvalidPatchRequestException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

class MergePatchDocumentAccessorTest {

  private final MergePatchDocumentAccessor mergePatchDocumentAccessor =
      new MergePatchDocumentAccessor();

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void currentDocumentReflectsTheCurrentPatchDocument() {
    setCurrentRequest("{\"title\":\"Updated\",\"stylesheet\":null}");
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();

    assertThat(document.field("title", "Updated").isPresent()).isTrue();
    assertThat(document.field("body", "ignored").isPresent()).isFalse();
    assertThat(document.field("stylesheet", null).isExplicitNull()).isTrue();
  }

  @Test
  void currentDocumentRejectsAnEmptyPatchDocument() {
    setCurrentRequest("{}");

    assertThatThrownBy(mergePatchDocumentAccessor::currentDocument)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH requests must include at least one field");
  }

  @Test
  void currentDocumentRejectsNonObjectBodies() {
    setCurrentRequest("[]");

    assertThatThrownBy(mergePatchDocumentAccessor::currentDocument)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH requests must use a JSON object body");
  }

  @Test
  void accessWithoutACachingWrapperIsRejectedPredictably() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThatThrownBy(mergePatchDocumentAccessor::currentDocument)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH request body could not be inspected");
  }

  private void setCurrentRequest(String body) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContent(body.getBytes(StandardCharsets.UTF_8));

    ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, 2_000_000);
    readBody(wrapper);

    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(wrapper));
  }

  private void readBody(HttpServletRequest request) {
    try {
      request.getInputStream().readAllBytes();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to prime cached request body", exception);
    }
  }
}
