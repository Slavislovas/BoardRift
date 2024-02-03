package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRetrievalDto {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private String dateOfBirth;
    private String username;
}
