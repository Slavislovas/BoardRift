package com.socialnetwork.boardrift.rest.model.post;

import com.socialnetwork.boardrift.repository.model.post.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPageDto {
    String nextPageUrl;
    List<Post> posts;
}
