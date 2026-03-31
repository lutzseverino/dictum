package dev.dictum.api.site.factory;

import dev.dictum.api.generated.model.UpdateSiteSettingsRequest;
import dev.dictum.api.site.model.patch.SiteSettingsPatch;
import dev.dictum.api.web.patch.MergePatchDocument;
import dev.dictum.api.web.patch.MergePatchDocumentAccessor;
import org.springframework.stereotype.Component;

@Component
public class SiteSettingsApiInputFactory {

  private final MergePatchDocumentAccessor mergePatchDocumentAccessor;

  SiteSettingsApiInputFactory(MergePatchDocumentAccessor mergePatchDocumentAccessor) {
    this.mergePatchDocumentAccessor = mergePatchDocumentAccessor;
  }

  public SiteSettingsPatch toPatch(UpdateSiteSettingsRequest request) {
    MergePatchDocument document = mergePatchDocumentAccessor.currentDocument();
    return new SiteSettingsPatch(
        document.field("title", request.getTitle()),
        document.field("subtitle", request.getSubtitle()),
        document.field("motd", request.getMotd()));
  }
}
