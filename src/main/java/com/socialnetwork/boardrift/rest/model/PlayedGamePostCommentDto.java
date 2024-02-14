package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGamePostCommentDto {
    private Long id;
    private String text;
    private String creationDate;
    private UserRetrievalMinimalDto commentCreator;
}
