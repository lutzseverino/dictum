package dev.dictum.api.site.mapper;

import dev.dictum.api.generated.model.SiteSettingsResponse;
import dev.dictum.api.site.model.state.SiteSettingsState;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SiteSettingsApiMapper {

  SiteSettingsResponse toResponse(SiteSettingsState state);
}
