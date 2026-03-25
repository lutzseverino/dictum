package dev.dictum.api.content.command;

import dev.dictum.api.generated.model.PostTemplate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CreatePostCommand(
    String title,
    String slug,
    String excerpt,
    PostTemplate template,
    @Nullable List<String> tags,
    String body,
    @Nullable String stylesheet) {

  public CreatePostCommand {
    tags = tags == null ? null : List.copyOf(tags);
  }
}
