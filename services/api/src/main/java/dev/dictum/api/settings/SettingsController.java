package dev.dictum.api.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Settings", description = "Stub site settings endpoints")
public class SettingsController {

  @GetMapping("/site")
  @Operation(summary = "Get placeholder site settings")
  public SiteSettingsResponse getSiteSettings() {
    return new SiteSettingsResponse(
        "Dictum",
        "A remotely steerable markdown blog kit.",
        "Skeleton mode is live: boundaries first, mutations later.",
        "external-content-repository");
  }
}
