package com.socialnetwork.boardrift.rest.model;

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
    private String description;
    private Instant creationDate;
    private Integer topScore;
    private Integer lowestScore;
    private Double averageScore;
    private Boolean statsAdded;
    private String stats;
    private UserRetrievalMinimalDto postCreator;
    private Set<UserRetrievalMinimalDto> players;
    private Set<PlayedGamePostCommentDto> comments;
    private Integer likes;
}
