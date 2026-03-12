package dev.dictum.api.providers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Preview of a provider-backed job in the control plane.")
public record ProviderJobSummaryResponse(
	String id,
	String provider,
	String state,
	String operation
) {
}
