package dev.dictum.api.settings;

import dev.dictum.api.generated.api.SiteSettingsApi;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class SiteSettingsController implements SiteSettingsApi {

  private final SiteSettingsQueryService siteSettingsQueryService;
  private final SiteSettingsCommandService siteSettingsCommandService;

  SiteSettingsController(
      SiteSettingsQueryService siteSettingsQueryService,
      SiteSettingsCommandService siteSettingsCommandService) {
    this.siteSettingsQueryService = siteSettingsQueryService;
    this.siteSettingsCommandService = siteSettingsCommandService;
  }

  @Override
  public ResponseEntity<SiteSettingsResponse> getSiteSettings() {
    return ResponseEntity.ok(siteSettingsQueryService.getResponse());
  }

  @Override
  public ResponseEntity<SiteSettingsResponse> updateSiteSettings(
      UpdateSiteSettingsRequest updateSiteSettingsRequest) {
    return ResponseEntity.ok(siteSettingsCommandService.update(updateSiteSettingsRequest));
  }
}
