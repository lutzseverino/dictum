package dev.dictum.api.content.model.vo;

import dev.dictum.api.generated.model.PostStatus;
import dev.dictum.api.generated.model.PostTemplate;
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
    String stylesheetContent,
    String contentPath,
    String stylesheetPath,
    String metaPath) {}
