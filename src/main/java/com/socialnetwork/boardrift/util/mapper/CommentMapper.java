package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostCommentEntity;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostCommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface CommentMapper {
    PlayedGamePostCommentDto playedGamePostCommentEntityToDto(PlayedGamePostCommentEntity entity);
}
