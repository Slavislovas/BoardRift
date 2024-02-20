package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollOptionRetrievalDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.simple_post.SimplePostRetrievalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface PostMapper {
    @Mapping(target = "likes", expression = "java(entity.getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getComments().size())")
    PlayedGamePostRetrievalDto playedGamePostEntityToRetrievalDto(PlayedGamePostEntity entity);

    @Mapping(target = "likes", expression = "java(entity.getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getComments().size())")
    SimplePostRetrievalDto simplePostEntityToRetrievalDto(SimplePostEntity entity);

    @Mapping(target = "alreadyVoted", expression = "java(alreadyVoted)")
    @Mapping(target = "options", qualifiedByName = "pollOptionEntityToRetrievalDto")
    PollPostRetrievalDto pollPostEntityToRetrievalDto(PollPostEntity entity, Boolean alreadyVoted);

    @Mapping(target = "votes", expression = "java(entity.getVotes().size())")
    @Named("pollOptionEntityToRetrievalDto")
    PollOptionRetrievalDto pollOptionEntityToRetrievalDto(PollOptionEntity entity);

    PostCommentDto postCommentEntityToDto(PostCommentEntity playedGamePostCommentEntity);
}
