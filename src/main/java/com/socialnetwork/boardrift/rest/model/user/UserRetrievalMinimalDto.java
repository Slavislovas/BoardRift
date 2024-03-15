package com.socialnetwork.boardrift.rest.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRetrievalMinimalDto {
    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String lastname;
    private String profilePictureUrl;
    private UserStatus status;
}
