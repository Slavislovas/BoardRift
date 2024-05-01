package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.service.AwsService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public abstract class PlayedGameMapper {
    @Autowired
    AwsService awsService;

    @Mapping(source = "entity", target = "associatedPlays", qualifiedByName = "associatedPlayEntitiesToDtos")
    @Mapping(source = "entity", target = "highestScore", qualifiedByName = "mapHighestScore")
    @Mapping(source = "entity", target = "lowestScore", qualifiedByName = "mapLowestScore")
    @Mapping(source = "entity", target = "averageScore", qualifiedByName = "mapAverageScore")
    @Mapping(target = "editable", expression = "java(entity.getAssociatedWith().isEmpty())")
    @Mapping(source = "entity", target = "ownerName", qualifiedByName = "mapOwnerName")
    public abstract PlayedGameDto entityToDto(PlayedGameEntity entity);

    @Named("associatedPlayEntitiesToDtos")
    public Set<PlayedGameDto> associatedPlayEntitiesToDtos(PlayedGameEntity playedGame) {
        Set<PlayedGameDto> plays = new HashSet<>();

        if (playedGame.getAssociatedPlays().size() != 0) {
            playedGame.getAssociatedPlays().forEach(play -> {
                plays.add(new PlayedGameDto(play.getId(), play.getBggGameId(), null, null, play.getScore(),
                        play.getWon(), play.getScoringSystem(), null, null, null, null,
                        new UserRetrievalMinimalDto(play.getUser().getId(), play.getUser().getName(), play.getUser().getLastname(),
                                play.getUser().getProfilePictureUrl(), null, null, null),
                        null, null, null));
            });
        }

        if (!playedGame.getAssociatedWith().isEmpty()) {
            playedGame.getAssociatedWith().forEach(play -> {
                plays.add(new PlayedGameDto(play.getId(), play.getBggGameId(), null, null, play.getScore(),
                        play.getWon(), play.getScoringSystem(), null, null, null, null,
                        new UserRetrievalMinimalDto(play.getUser().getId(), play.getUser().getName(), play.getUser().getLastname(),
                                play.getUser().getProfilePictureUrl(), null, null, null),
                        null, null, null));
            });
        }

        if (!playedGame.getAssociatedWith().isEmpty()) {
            playedGame.getAssociatedWith().forEach(play -> {
                for (PlayedGameEntity associatedPlay : play.getAssociatedPlays()) {
                    if (!playedGame.getId().equals(associatedPlay.getId())) {
                        plays.add(new PlayedGameDto(associatedPlay.getId(), associatedPlay.getBggGameId(), null, null, associatedPlay.getScore(),
                                associatedPlay.getWon(), associatedPlay.getScoringSystem(), null, null, null, null,
                                new UserRetrievalMinimalDto(associatedPlay.getUser().getId(), associatedPlay.getUser().getName(), associatedPlay.getUser().getLastname(),
                                        associatedPlay.getUser().getProfilePictureUrl(), null, null, null),
                                null, null, null));
                    }
                }
            });
        }

        return plays;
    }

    @Named("mapOwnerName")
    public String mapOwnerName(PlayedGameEntity playedGame) {
        if (playedGame.getAssociatedWith().isEmpty()) {
            return null;
        }

        PlayedGameEntity ownerPlay = playedGame.getAssociatedWith().stream().findFirst().get();



        return ownerPlay.getUser().getName() + " " + ownerPlay.getUser().getLastname();
    }

    @Named("mapHighestScore")
    public Integer mapHighestScore(PlayedGameEntity playedGame) {
        if (playedGame.getScoringSystem().equals("no-score")) {
            return null;
        }

       Integer highestScore = playedGame.getScore();

        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedPlays()) {
            if (highestScore < associatedPlay.getScore()) {
                highestScore = associatedPlay.getScore();
            }
        }

        return highestScore;
    }

    @Named("mapLowestScore")
    public Integer mapLowestScore(PlayedGameEntity playedGame) {
        if (playedGame.getScoringSystem().equals("no-score")) {
            return null;
        }

        Integer lowestScore = playedGame.getScore();

        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedPlays()) {
            if (lowestScore > associatedPlay.getScore()) {
                lowestScore = associatedPlay.getScore();
            }
        }

        return lowestScore;
    }

    @Named("mapAverageScore")
    public Double mapAverageScore(PlayedGameEntity playedGame) {
        if (playedGame.getScoringSystem().equals("no-score")) {
            return null;
        }

        Double average = playedGame.getScore().doubleValue();

        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedPlays()) {
            average += associatedPlay.getScore().doubleValue();
        }

        return Math.round((average / (playedGame.getAssociatedPlays().size() + 1)) * 100.0) / 100.0;
    }
}
