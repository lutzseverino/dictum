package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.mapper.SiteSettingsApiMapper;
import dev.dictum.api.site.repository.SiteSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsQueryService {

  private final SiteSettingsRepository siteSettingsRepository;
  private final SiteSettingsApiMapper siteSettingsApiMapper;

  SiteSettingsQueryService(
      SiteSettingsRepository siteSettingsRepository, SiteSettingsApiMapper siteSettingsApiMapper) {
    this.siteSettingsRepository = siteSettingsRepository;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
  }

  public SiteSettingsResponse getResponse() {
    return siteSettingsApiMapper.toResponse(siteSettingsRepository.get());
  }
}
