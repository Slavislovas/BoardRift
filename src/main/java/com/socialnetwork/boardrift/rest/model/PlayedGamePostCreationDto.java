package com.socialnetwork.boardrift.rest.model;

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
   private Integer postCreatorPlace;
   private Integer postCreatorPoints;
   private Boolean statsAdded;
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
       private Integer place;
       private Integer points;
   }
}
