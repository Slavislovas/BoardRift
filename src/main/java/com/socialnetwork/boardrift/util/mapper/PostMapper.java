package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostRetrievalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface PostMapper {
    @Mapping(target = "likes", expression = "java(entity.getLikes().size())")
    PlayedGamePostRetrievalDto playedGamePostEntityToRetrievalDto(PlayedGamePostEntity entity);
}
