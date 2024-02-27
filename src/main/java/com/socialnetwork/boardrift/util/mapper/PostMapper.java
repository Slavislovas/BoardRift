package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface PostMapper {
    @Mapping(target = "likes", expression = "java(entity.getBasePost().getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getBasePost().getComments().size())")
    @Mapping(target = "description", expression = "java(entity.getBasePost().getDescription())")
    @Mapping(target = "creationDate", expression = "java(entity.getBasePost().getCreationDate())")
    @Mapping(target = "postCreator", expression = "java(userMapper.entityToMinimalRetrievalDto(entity.getBasePost().getPostCreator()))")
    PlayedGamePostRetrievalDto playedGamePostEntityToRetrievalDto(PlayedGamePostEntity entity);

    @Mapping(target = "likes", expression = "java(entity.getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getComments().size())")
    SimplePostRetrievalDto simplePostEntityToRetrievalDto(SimplePostEntity entity);

    @Mapping(target = "likes", expression = "java(entity.getBasePost().getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getBasePost().getComments().size())")
    @Mapping(target = "description", expression = "java(entity.getBasePost().getDescription())")
    @Mapping(target = "creationDate", expression = "java(entity.getBasePost().getCreationDate())")
    @Mapping(target = "options", qualifiedByName = "pollOptionEntityToRetrievalDto")
    @Mapping(target = "postCreator", expression = "java(userMapper.entityToMinimalRetrievalDto(entity.getBasePost().getPostCreator()))")
    PollPostRetrievalDto pollPostEntityToRetrievalDto(PollPostEntity entity);

    @Mapping(target = "votes", expression = "java(entity.getVotes().size())")
    @Named("pollOptionEntityToRetrievalDto")
    PollOptionRetrievalDto pollOptionEntityToRetrievalDto(PollOptionEntity entity);

    PostCommentDto postCommentEntityToDto(PostCommentEntity playedGamePostCommentEntity);
}
