package com.socialnetwork.boardrift.rest.model.played_game_post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayedGamePostCreationDto {
   private Long playedGameId;
   private String description;
   private Boolean postCreatorWon;
   private Integer postCreatorPoints;
   private String scoringSystem;
   private Set<SelectedPlayerDto> players;

    public void addPlayer(SelectedPlayerDto selectedPlayerDto) {
        players.add(selectedPlayerDto);
    }

    @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class SelectedPlayerDto {
       private Long id;
       private String name;
       private String lastname;
       private Boolean won;
       private Integer points;
   }
}
