package com.socialnetwork.boardrift.rest.model.simple_post;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePostCreationDto {
    @NotBlank(message = "Description is required")
    private String description;
}
