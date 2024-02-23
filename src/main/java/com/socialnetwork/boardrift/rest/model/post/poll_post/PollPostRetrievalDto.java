package com.socialnetwork.boardrift.rest.model.post.poll_post;

import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
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
    private String question;
    private Date creationDate;
    private UserRetrievalMinimalDto postCreator;
    private Set<PollOptionRetrievalDto> options;
    private Boolean alreadyVoted = false;
    private PollOptionRetrievalDto selectedOption;
    private String type = "poll";
}
