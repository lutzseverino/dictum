package dev.dictum.api.settings;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Site-level settings that will eventually come from the external content repository.")
public record SiteSettingsResponse(String title, String subtitle, String motd, String source) {}
