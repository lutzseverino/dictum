package dev.dictum.api.posts;

import dev.dictum.api.generated.model.PostResponse;
import dev.dictum.api.generated.model.PostSummary;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PostApiMapper {

  PostSummary toSummary(PostState state);

  List<PostSummary> toSummaries(List<PostState> states);

  PostResponse toResponse(PostState state);
}
