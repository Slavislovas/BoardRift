package com.socialnetwork.boardrift.rest.model;

import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayedGamePageDto {
    String nextPageUrl;
    List<PlayedGameDto> plays;
}
