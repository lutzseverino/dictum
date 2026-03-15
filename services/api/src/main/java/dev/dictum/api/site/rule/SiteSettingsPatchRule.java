package dev.dictum.api.site.rule;

import dev.dictum.api.site.model.vo.SiteSettingsPatch;

public interface SiteSettingsPatchRule {

  void validate(SiteSettingsPatch patch);
}
