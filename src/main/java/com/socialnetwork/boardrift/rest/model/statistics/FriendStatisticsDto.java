package com.socialnetwork.boardrift.rest.model.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendStatisticsDto {
    private UserRetrievalMinimalDto userData;
    private Integer timesPlayedWith;
}
