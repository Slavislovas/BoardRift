package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardGameDto {
    private Long id;
    private String name;
    private Integer yearPublished;
    private Integer gameDifficultyRating;
    private Integer minimumPlayers;
    private Integer maximumPlayers;
    private String imagePath;
}
