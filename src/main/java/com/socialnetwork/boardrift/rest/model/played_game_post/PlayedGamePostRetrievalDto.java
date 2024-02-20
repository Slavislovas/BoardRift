package com.socialnetwork.boardrift.rest.model.played_game_post;

import com.socialnetwork.boardrift.rest.model.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGamePostRetrievalDto {
    private Long id;
    private Long bggGameId;
    private String gameName;
    private String gamePictureUrl;
    private String description;
    private String scoringSystem;
    private Instant creationDate;
    private Integer highestScore;
    private Integer lowestScore;
    private Double averageScore;
    private UserRetrievalMinimalDto postCreator;
    private Set<PlayedGameDto> plays;
    private Integer comments;
    private Integer likes;
    private Boolean alreadyLiked = false;
    private String type = "played-game";
}
