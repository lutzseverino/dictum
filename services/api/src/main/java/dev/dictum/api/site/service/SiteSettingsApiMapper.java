package dev.dictum.api.site.service;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.model.vo.SiteSettingsState;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SiteSettingsApiMapper {

  SiteSettingsResponse toResponse(SiteSettingsState state);
}
