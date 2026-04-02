package dev.dictum.api.site.controller;

import dev.dictum.api.generated.api.SiteSettingsApi;
import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.factory.SiteSettingsApiInputFactory;
import dev.dictum.api.site.mapper.SiteSettingsApiMapper;
import dev.dictum.api.site.service.SiteSettingsCommandService;
import dev.dictum.api.site.service.SiteSettingsQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class SiteSettingsController implements SiteSettingsApi {

  private final SiteSettingsApiInputFactory siteSettingsApiInputFactory;
  private final SiteSettingsApiMapper siteSettingsApiMapper;
  private final SiteSettingsQueryService siteSettingsQueryService;
  private final SiteSettingsCommandService siteSettingsCommandService;

  SiteSettingsController(
      SiteSettingsApiInputFactory siteSettingsApiInputFactory,
      SiteSettingsApiMapper siteSettingsApiMapper,
      SiteSettingsQueryService siteSettingsQueryService,
      SiteSettingsCommandService siteSettingsCommandService) {
    this.siteSettingsApiInputFactory = siteSettingsApiInputFactory;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
    this.siteSettingsQueryService = siteSettingsQueryService;
    this.siteSettingsCommandService = siteSettingsCommandService;
  }

  @Override
  public ResponseEntity<SiteSettingsResponse> getSiteSettings() {
    return ResponseEntity.ok(siteSettingsApiMapper.toResponse(siteSettingsQueryService.get()));
  }

  @Override
  public ResponseEntity<SiteSettingsResponse> updateSiteSettings(
      String xCsrfToken, UpdateSiteSettingsRequest updateSiteSettingsRequest) {
    return ResponseEntity.ok(
        siteSettingsApiMapper.toResponse(
            siteSettingsCommandService.update(
                siteSettingsApiInputFactory.toPatch(updateSiteSettingsRequest))));
  }
}
