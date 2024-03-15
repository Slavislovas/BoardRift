package com.socialnetwork.boardrift.rest.model.post.poll_post;

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
public class PollPostRetrievalDto implements Post {
    private Long id;
    private String description;
    private Date creationDate;
    private UserRetrievalMinimalDto postCreator;
    private Set<PollOptionRetrievalDto> options;
    private Boolean alreadyVoted = false;
    private Boolean isEditable = true;
    private PollOptionRetrievalDto selectedOption;
    private String type = "poll";
    private Integer likes;
    private Integer comments;
    private Boolean alreadyLiked = false;
}
