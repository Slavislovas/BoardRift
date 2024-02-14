package com.socialnetwork.boardrift.rest.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
