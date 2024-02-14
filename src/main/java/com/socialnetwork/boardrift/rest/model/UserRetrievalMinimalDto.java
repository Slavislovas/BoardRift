package com.socialnetwork.boardrift.rest.model;

import com.socialnetwork.boardrift.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
