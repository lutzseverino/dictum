package dev.dictum.api.commands;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Description of a control-plane command planned for the admin app.")
public record CommandDefinitionResponse(
    String name, String target, String status, String description) {}
