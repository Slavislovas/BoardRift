package com.socialnetwork.boardrift.rest.model.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStatisticsDto {
    private Long bggGameId;
    private String gamePictureUrl;
    private String gameName;
    private String category;
    private Integer highestScore;
    private Integer lowestScore;
    private Integer amountOfTimesWon;
    private Integer amountOfTimesLost;
    private Integer currentWinStreak;
    private Integer longestWinStreak;
    private Integer longestLossStreak;

    public Integer getTotalPlays() {
        return amountOfTimesWon + amountOfTimesLost;
    }

    public void incrementAmountOfTimesWon() {
        this.amountOfTimesWon++;
    }

    public void incrementAmountOfTimesLost() {
        this.amountOfTimesLost++;
    }
}