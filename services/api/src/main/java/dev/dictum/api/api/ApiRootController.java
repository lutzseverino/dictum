package dev.dictum.api.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Root", description = "High-level API contract discovery")
public class ApiRootController {

	@GetMapping
	@Operation(summary = "Describe the skeleton route groups")
	public ApiRootResponse index() {
		return new ApiRootResponse(
			"dictum-api",
			List.of("posts", "settings", "commands", "provider-jobs"),
			"/swagger-ui.html",
			"/actuator/health"
		);
	}

	public record ApiRootResponse(
		String service,
		List<String> routeGroups,
		String docsPath,
		String healthPath
	) {
	}
}

