package com.socialnetwork.boardrift.rest.model.poll_post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollOptionRetrievalDto {
    private Long id;
    private String text;
    private Integer votes;
}
