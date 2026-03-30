package dev.dictum.api.content.model.state;

import dev.dictum.api.content.model.vo.PostStatus;
import dev.dictum.api.content.model.vo.PostTemplate;
import java.time.LocalDate;
import java.util.List;

public record PostState(
    String slug,
    String title,
    String excerpt,
    PostStatus status,
    PostTemplate template,
    LocalDate publishedAt,
    List<String> tags,
    boolean hasStylesheet,
    String body,
    String stylesheetContent) {}
