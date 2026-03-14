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

class MergePatchBodyAccessorTest {

  private final MergePatchBodyAccessor mergePatchBodyAccessor = new MergePatchBodyAccessor();

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void containsFieldAndExplicitNullReflectTheCurrentPatchDocument() {
    setCurrentRequest("{\"title\":\"Updated\",\"stylesheet\":null}");

    assertThat(mergePatchBodyAccessor.containsField("title")).isTrue();
    assertThat(mergePatchBodyAccessor.containsField("body")).isFalse();
    assertThat(mergePatchBodyAccessor.isExplicitNull("stylesheet")).isTrue();
  }

  @Test
  void requireAnyFieldRejectsAnEmptyPatchDocument() {
    setCurrentRequest("{}");

    assertThatThrownBy(mergePatchBodyAccessor::requireAnyField)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH requests must include at least one field");
  }

  @Test
  void requireAnyFieldRejectsNonObjectBodies() {
    setCurrentRequest("[]");

    assertThatThrownBy(mergePatchBodyAccessor::requireAnyField)
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH requests must use a JSON object body");
  }

  @Test
  void accessWithoutACachingWrapperIsRejectedPredictably() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThatThrownBy(() -> mergePatchBodyAccessor.containsField("title"))
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
