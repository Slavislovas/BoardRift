package com.socialnetwork.boardrift.rest.model.poll_post;

import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollPostRetrievalDto {
    private Long id;
    private String question;
    private Date creationDate;
    private UserRetrievalMinimalDto postCreator;
    private Set<PollOptionRetrievalDto> options;
    private Boolean alreadyVoted;
    private String type = "poll";
}
