package dev.dictum.api.posts;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary view of a markdown-backed post contract.")
public record PostSummaryResponse(
	String slug,
	String title,
	String status,
	String template,
	String contentPath,
	boolean hasStylesheet
) {
}
