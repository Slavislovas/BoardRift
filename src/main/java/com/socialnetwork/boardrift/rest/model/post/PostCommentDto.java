package com.socialnetwork.boardrift.rest.model.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {
    private Long id;
    @NotBlank(message = "Comment cannot be empty")
    private String text;
    private String creationDate;
    private UserRetrievalMinimalDto commentCreator;
    private Boolean alreadyReported;
    private String postType;
    private Long postId;
    private List<ReportDto> reports;
}
