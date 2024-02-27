package com.socialnetwork.boardrift.rest.model.post.poll_post;

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
    @NotBlank(message = "Description is required")
    private String description;

    @NotEmpty(message = "At least one option is required")
    private List<PollOptionDto> options;
}
