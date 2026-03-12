package dev.dictum.api.commands;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commands")
@Tag(name = "Commands", description = "Stub command catalog for future remote actions")
public class CommandsController {

	@GetMapping
	@Operation(summary = "List planned remote-control commands")
	public CommandsResponse listCommands() {
		return new CommandsResponse(List.of(
			new CommandDefinitionResponse(
				"create_post",
				"posts",
				"planned",
				"Create a new markdown-backed entry in the external content repository."
			),
			new CommandDefinitionResponse(
				"publish_post",
				"posts",
				"planned",
				"Move a draft post into the published state."
			),
			new CommandDefinitionResponse(
				"set_subtitle",
				"settings",
				"planned",
				"Change the public homepage subtitle."
			),
			new CommandDefinitionResponse(
				"set_motd",
				"settings",
				"planned",
				"Update the site-wide message of the day."
			)
		));
	}

	public record CommandsResponse(List<CommandDefinitionResponse> commands) {
	}
}

