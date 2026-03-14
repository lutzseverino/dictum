package dev.dictum.api.web.patch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.web.error.InvalidPatchRequestException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class MergePatchDocumentTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void fieldExtractionPreservesPresenceExplicitNullAndValue() throws IOException {
    MergePatchDocument document =
        new MergePatchDocument(
            OBJECT_MAPPER.readTree("{\"title\":\"Updated\",\"stylesheet\":null}"));

    PatchValue<String> title = document.field("title", "Updated");
    PatchValue<String> body = document.field("body", null);
    PatchValue<String> stylesheet = document.field("stylesheet", null);

    assertThat(title.isPresent()).isTrue();
    assertThat(title.isExplicitNull()).isFalse();
    assertThat(title.value()).isEqualTo("Updated");
    assertThat(body.isPresent()).isFalse();
    assertThat(stylesheet.isExplicitNull()).isTrue();
  }

  @Test
  void constructorRejectsEmptyObjects() throws IOException {
    assertThatThrownBy(() -> new MergePatchDocument(OBJECT_MAPPER.readTree("{}")))
        .isInstanceOf(InvalidPatchRequestException.class)
        .hasMessage("PATCH requests must include at least one field");
  }
}
