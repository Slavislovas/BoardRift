package com.socialnetwork.boardrift.rest.model.poll_post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollPostCreationDto {
    private String question;
    private List<PollOptionDto> options;
}
