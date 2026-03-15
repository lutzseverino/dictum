package dev.dictum.api.site.rule;

import dev.dictum.api.site.model.vo.SiteSettingsPatch;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SiteSettingsPatchValidator {

  private final List<SiteSettingsPatchRule> rules;

  public SiteSettingsPatchValidator(List<SiteSettingsPatchRule> rules) {
    this.rules = List.copyOf(rules);
  }

  public void validate(SiteSettingsPatch patch) {
    rules.forEach(rule -> rule.validate(patch));
  }
}
