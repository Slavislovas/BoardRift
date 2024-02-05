package com.socialnetwork.boardrift.rest.model;

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
    private String dateOfBirth;
    private String profilePictureUrl;
}
