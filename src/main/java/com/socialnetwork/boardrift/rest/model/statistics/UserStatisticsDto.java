package com.socialnetwork.boardrift.rest.model.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDto {
    private GameStatisticsDto favouriteGame;
    private Integer totalGamesWon;
    private Integer totalGamesLost;
    private FriendStatisticsDto favouriteFriend;
    private List<GameStatisticsDto> gameStatistics;
}
