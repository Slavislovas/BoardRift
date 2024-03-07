package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface PlayedGameMapper {

    @Mapping(target = "gameName", expression = "java(gameName)")
    @Mapping(target = "gamePictureUrl", expression = "java(gamePictureUrl)")
    @Mapping(source = "entity", target = "associatedPlays", qualifiedByName = "associatedPlayEntitiesToDtos")
    @Mapping(source = "entity", target = "highestScore", qualifiedByName = "mapHighestScore")
    @Mapping(source = "entity", target = "lowestScore", qualifiedByName = "mapLowestScore")
    @Mapping(source = "entity", target = "averageScore", qualifiedByName = "mapAverageScore")
    PlayedGameDto entityToDto(PlayedGameEntity entity, String gameName, String gamePictureUrl);

    @Named("associatedPlayEntitiesToDtos")
    public static Set<PlayedGameDto> associatedPlayEntitiesToDtos(PlayedGameEntity playedGame) {
        Set<PlayedGameDto> plays = new HashSet<>();

        playedGame.getAssociatedPlays().stream().forEach(play -> {
            plays.add(new PlayedGameDto(play.getId(), play.getBggGameId(), null, null, play.getScore(),
                    play.getWon(), play.getScoringSystem(), null, null, null, null,
                    new UserRetrievalMinimalDto(play.getUser().getId(), play.getUser().getName(), play.getUser().getLastname(),
                            play.getUser().getProfilePictureUrl(), play.getUser().getStatus()), null));
        });

        return plays;
    }

    @Named("mapHighestScore")
    public static Integer mapHighestScore(PlayedGameEntity playedGame) {
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
    public static Integer mapLowestScore(PlayedGameEntity playedGame) {
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
    public static Double mapAverageScore(PlayedGameEntity playedGame) {
        if (playedGame.getScoringSystem().equals("no-score")) {
            return null;
        }

        Double average = playedGame.getScore().doubleValue();

        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedPlays()) {
            average += associatedPlay.getScore().doubleValue();
        }

        return average / (playedGame.getAssociatedPlays().size() + 1);
    }
}
