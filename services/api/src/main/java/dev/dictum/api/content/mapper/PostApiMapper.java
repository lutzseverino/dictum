package dev.dictum.api.content.mapper;

import dev.dictum.api.content.model.vo.PostState;
import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostApiMapper {

  PostSummary toSummary(PostState state);

  List<PostSummary> toSummaries(List<PostState> states);

  PostResponse toResponse(PostState state);
}
