package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.mapper.SiteSettingsApiMapper;
import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import dev.dictum.api.site.rule.SiteSettingsPatchValidator;
import dev.dictum.api.site.store.SiteSettingsStore;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsCommandService {

  private final SiteSettingsStore siteSettingsStore;
  private final SiteSettingsApiMapper siteSettingsApiMapper;
  private final SiteSettingsPatchValidator siteSettingsPatchValidator;
  private final MergePatchDocumentAccessor mergePatchDocumentAccessor;

  SiteSettingsCommandService(
      SiteSettingsStore siteSettingsStore,
      SiteSettingsApiMapper siteSettingsApiMapper,
      SiteSettingsPatchValidator siteSettingsPatchValidator,
      MergePatchDocumentAccessor mergePatchDocumentAccessor) {
    this.siteSettingsStore = siteSettingsStore;
    this.siteSettingsApiMapper = siteSettingsApiMapper;
    this.siteSettingsPatchValidator = siteSettingsPatchValidator;
    this.mergePatchDocumentAccessor = mergePatchDocumentAccessor;
  }

  public SiteSettingsResponse update(UpdateSiteSettingsRequest request) {
    SiteSettingsState current = siteSettingsStore.get();
    SiteSettingsPatch patch = readPatch(request);
    siteSettingsPatchValidator.validate(patch);

    SiteSettingsState updated =
        new SiteSettingsState(
            patch.title().isPresent() ? patch.title().value() : current.title(),
            patch.subtitle().isPresent() ? patch.subtitle().value() : current.subtitle(),
            patch.motd().isPresent() ? patch.motd().value() : current.motd());

    return siteSettingsApiMapper.toResponse(siteSettingsStore.save(updated));
  }

  private SiteSettingsPatch readPatch(UpdateSiteSettingsRequest request) {
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();
    return SiteSettingsPatch.from(request, document);
  }
}
