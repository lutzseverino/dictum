package dev.dictum.api.providers;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Declared provider adapter and its supported command surface.")
public record ProviderStatusResponse(
	String provider,
	String mode,
	List<String> supportedCommands
) {
}
