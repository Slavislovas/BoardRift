package com.socialnetwork.boardrift.rest.model.simple_post;

import com.socialnetwork.boardrift.rest.model.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePostRetrievalDto {
    private Long id;
    private String description;
    private Instant creationDate;
    private UserRetrievalMinimalDto postCreator;
    private Set<PostCommentDto> comments;
    private Integer likes;
    private String type = "simple";
}
