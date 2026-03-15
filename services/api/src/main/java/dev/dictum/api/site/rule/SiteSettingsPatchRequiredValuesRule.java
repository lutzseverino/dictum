package dev.dictum.api.site.rule;

import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import org.springframework.stereotype.Component;

@Component
public class SiteSettingsPatchRequiredValuesRule implements SiteSettingsPatchRule {

  @Override
  public void validate(SiteSettingsPatch patch) {
    patch.title().requireNonNullWhenPresent("title");
    patch.subtitle().requireNonNullWhenPresent("subtitle");
    patch.motd().requireNonNullWhenPresent("motd");
  }
}
