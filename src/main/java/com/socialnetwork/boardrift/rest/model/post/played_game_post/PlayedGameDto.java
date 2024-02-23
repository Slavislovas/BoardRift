package com.socialnetwork.boardrift.rest.model.post.played_game_post;

import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGameDto {
    private Long id;
    private Long bggGameId;
    private Integer score;
    private Boolean won;
    private String scoringSystem;
    private UserRetrievalMinimalDto user;
}
