package com.socialnetwork.boardrift.rest.model.post;

import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@NotBlank
public class PostCommentPageDto {
    private String nextPageUrl;
    private List<PostCommentDto> comments;
}
