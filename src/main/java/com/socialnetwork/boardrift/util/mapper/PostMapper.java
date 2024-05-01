package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentReportEntity;
import com.socialnetwork.boardrift.repository.model.post.PostReportEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public abstract class PostMapper {

    @Mapping(target = "likes", expression = "java(entity.getBasePost().getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getBasePost().getComments().size())")
    @Mapping(target = "description", expression = "java(entity.getBasePost().getDescription())")
    @Mapping(target = "creationDate", expression = "java(entity.getBasePost().getCreationDate())")
    @Mapping(target = "postCreator", expression = "java(userMapper.entityToMinimalRetrievalDto(entity.getBasePost().getPostCreator()))")
    @Mapping(target = "postCreatorWon", expression = "java(entity.getPlayedGame().getWon())")
    @Mapping(target = "postCreatorPoints", expression = "java(entity.getPlayedGame().getScore())")
    @Mapping(target = "bggGameId", expression = "java(entity.getPlayedGame().getBggGameId())")
    @Mapping(target = "gameName", expression = "java(entity.getPlayedGame().getGameName())")
    @Mapping(target = "gamePictureUrl", expression = "java(entity.getPlayedGame().getGamePictureUrl())")
    @Mapping(source = "playedGame", target = "plays", qualifiedByName = "playToPlayedGamePostPlaysRetrievalDto")
    @Mapping(target = "reports", expression = "java(entity.getBasePost().getReports().stream().map(report -> postReportEntityToDto(report)).toList())")
   public abstract PlayedGamePostRetrievalDto playedGamePostEntityToRetrievalDto(PlayedGamePostEntity entity);

    @Mapping(target = "likes", expression = "java(entity.getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getComments().size())")
    public abstract SimplePostRetrievalDto simplePostEntityToRetrievalDto(SimplePostEntity entity);

    @Mapping(target = "likes", expression = "java(entity.getBasePost().getLikes().size())")
    @Mapping(target = "comments", expression = "java(entity.getBasePost().getComments().size())")
    @Mapping(target = "description", expression = "java(entity.getBasePost().getDescription())")
    @Mapping(target = "creationDate", expression = "java(entity.getBasePost().getCreationDate())")
    @Mapping(target = "options", qualifiedByName = "pollOptionEntityToRetrievalDto")
    @Mapping(target = "postCreator", expression = "java(userMapper.entityToMinimalRetrievalDto(entity.getBasePost().getPostCreator()))")
    @Mapping(source = "entity", target = "isEditable", qualifiedByName = "isPollPostEditable")
    @Mapping(target = "reports", expression = "java(entity.getBasePost().getReports().stream().map(report -> postReportEntityToDto(report)).toList())")
   public abstract PollPostRetrievalDto pollPostEntityToRetrievalDto(PollPostEntity entity);

    @Mapping(target = "votes", expression = "java(entity.getVotes().size())")
    @Named("pollOptionEntityToRetrievalDto")
   public abstract PollOptionRetrievalDto pollOptionEntityToRetrievalDto(PollOptionEntity entity);

    @Named("isPollPostEditable")
    public Boolean isPollPostEditable(PollPostEntity entity) {
        return entity
                .getOptions()
                .stream()
                .allMatch(pollOptionEntity -> pollOptionEntity.getVotes().isEmpty());
    };

    @Named("playToPlayedGamePostPlaysRetrievalDto")
    public Set<PlayedGameDto> playToPlayedGamePostPlaysRetrievalDto(PlayedGameEntity playedGame) {
        Set<PlayedGameDto> plays = new HashSet<>();

        plays.add(new PlayedGameDto(playedGame.getId(), playedGame.getBggGameId(),
                null, null, playedGame.getScore(),
                playedGame.getWon(), playedGame.getScoringSystem(),
                null, null, null, null,
                new UserRetrievalMinimalDto(playedGame.getUser().getId(), playedGame.getUser().getName(), playedGame.getUser().getLastname(),
                        playedGame.getUser().getProfilePictureUrl(), null, null, null), null,
                null, null));

        if (!playedGame.getAssociatedPlays().isEmpty()) {
            playedGame.getAssociatedPlays().stream().forEach(play -> {
                if (play.getUser().isEnabled()) {
                    plays.add(new PlayedGameDto(play.getId(), play.getBggGameId(),
                            null, null, play.getScore(),
                            play.getWon(), play.getScoringSystem(),
                            null, null, null, null,
                            new UserRetrievalMinimalDto(play.getUser().getId(), play.getUser().getName(), play.getUser().getLastname(),
                                    play.getUser().getProfilePictureUrl(), null, null, null), null,
                            null, null));
                }
            });
        }

        if (!playedGame.getAssociatedWith().isEmpty()) {
            playedGame.getAssociatedWith().stream().forEach(play -> {
                plays.add(new PlayedGameDto(play.getId(), play.getBggGameId(),
                        null, null, play.getScore(),
                        play.getWon(), play.getScoringSystem(),
                        null, null, null, null,
                        new UserRetrievalMinimalDto(play.getUser().getId(), play.getUser().getName(), play.getUser().getLastname(),
                                play.getUser().getProfilePictureUrl(), null, null, null), null,
                        null, null));
            });
        }

        return plays;
    }

    public abstract PostCommentDto postCommentEntityToDto(PostCommentEntity playedGamePostCommentEntity);
    public abstract ReportDto postReportEntityToDto(PostReportEntity entity);
    public abstract ReportDto postCommentReportEntityToDto(PostCommentReportEntity save);
}
