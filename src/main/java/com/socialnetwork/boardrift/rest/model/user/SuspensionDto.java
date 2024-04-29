package com.socialnetwork.boardrift.rest.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SuspensionDto {
    private Integer daysOfSuspension;
    private String reason;
}
