package com.socialnetwork.boardrift.rest.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRetrievalMinimalDto {
    private Long id;
    private String name;
    private String lastname;
    private String profilePictureUrl;
    private UserStatus status;
}
