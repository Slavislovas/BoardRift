package com.socialnetwork.boardrift.rest.model.post.played_game_post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGamePostCreationDto {
   @NotNull(message = "Played game id is required")
   private Long playedGameId;

   private String description;

   @NotNull(message = "Post creator won variable required")
   private Boolean postCreatorWon;

   private Integer postCreatorPoints;

   @NotBlank(message = "Scoring system is required")
   private String scoringSystem;

   private Set<SelectedPlayerDto> players;

    public void addPlayer(SelectedPlayerDto selectedPlayerDto) {
        players.add(selectedPlayerDto);
    }

    @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class SelectedPlayerDto {
       @NotNull(message = "Player id cannot be null")
       private Long id;

       @NotBlank(message = "Player name is required")
       private String name;

       @NotBlank(message = "Player lastname is required")
       private String lastname;

       @NotNull(message = "Won is required")
       private Boolean won;

       private Integer points;
   }
}
