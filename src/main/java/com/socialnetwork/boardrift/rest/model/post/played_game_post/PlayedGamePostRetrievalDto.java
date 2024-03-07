package com.socialnetwork.boardrift.rest.model.post.played_game_post;

import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGamePostRetrievalDto implements Post {
    private Long id;
    private Long bggGameId;
    private String gameName;
    private String gamePictureUrl;
    private String description;
    private String scoringSystem;
    private Date creationDate;
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
