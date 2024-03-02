package com.socialnetwork.boardrift.rest.model.post.played_game_post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGameDto {
    private Long id;
    private Long bggGameId;
    private String gameName;
    private String gamePictureUrl;
    private Integer score;
    private Boolean won;
    private String scoringSystem;
    private Integer highestScore;
    private Integer lowestScore;
    private Double averageScore;
    private Date creationDate;
    private UserRetrievalMinimalDto user;
    private Set<PlayedGameDto> associatedPlays;
}
