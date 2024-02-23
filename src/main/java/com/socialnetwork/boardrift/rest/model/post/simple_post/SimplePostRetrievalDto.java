package com.socialnetwork.boardrift.rest.model.post.simple_post;

import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePostRetrievalDto implements Post {
    private Long id;
    private String description;
    private Date creationDate;
    private UserRetrievalMinimalDto postCreator;
    private Integer comments;
    private Integer likes;
    private Boolean alreadyLiked = false;
    private String type = "simple";
}
