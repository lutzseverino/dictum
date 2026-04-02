package dev.dictum.api.auth.mapper;

import dev.dictum.api.auth.model.state.SessionState;
import dev.dictum.api.generated.model.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SessionApiMapper {

  SessionResponse toResponse(SessionState sessionState);
}
