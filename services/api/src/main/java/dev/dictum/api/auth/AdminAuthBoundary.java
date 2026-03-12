package dev.dictum.api.auth;

public record AdminAuthBoundary(
	String status,
	String note
) {

	public static AdminAuthBoundary stub() {
		return new AdminAuthBoundary(
			"deferred",
			"Authentication is intentionally left as a boundary in the skeleton slice."
		);
	}
}
