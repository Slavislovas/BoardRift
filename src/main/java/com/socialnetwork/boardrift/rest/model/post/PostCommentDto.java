package com.socialnetwork.boardrift.rest.model.post;

import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {
    private Long id;
    @NotBlank(message = "Comment cannot be empty")
    private String text;
    private String creationDate;
    private UserRetrievalMinimalDto commentCreator;
}
