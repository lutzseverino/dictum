package dev.dictum.api.site.model.patch;

import dev.dictum.api.site.model.state.SiteSettingsState;
import dev.dictum.api.web.patch.PatchValue;

public record SiteSettingsPatch(
    PatchValue<String> title, PatchValue<String> subtitle, PatchValue<String> motd) {

  public void validate() {
    title().requireNonNullWhenPresent("title");
    subtitle().requireNonNullWhenPresent("subtitle");
    motd().requireNonNullWhenPresent("motd");
  }

  public SiteSettingsState applyTo(SiteSettingsState current) {
    return new SiteSettingsState(
        title().isPresent() ? title().value() : current.title(),
        subtitle().isPresent() ? subtitle().value() : current.subtitle(),
        motd().isPresent() ? motd().value() : current.motd());
  }
}
