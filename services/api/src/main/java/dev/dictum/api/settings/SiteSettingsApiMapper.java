package dev.dictum.api.settings;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SiteSettingsApiMapper {

  SiteSettingsResponse toResponse(SiteSettingsState state);
}
