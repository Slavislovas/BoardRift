package com.socialnetwork.boardrift.rest.model.poll_post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollPostCreationDto {
    @NotBlank(message = "Question is required")
    private String question;

    @NotEmpty(message = "At least one option is required")
    private List<PollOptionDto> options;
}
