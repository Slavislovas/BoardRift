package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private String profilePictureUrl;
    private Boolean publicPosts;
    private Boolean publicFriendsList;
    private Boolean publicPlays;
    private Boolean userAlreadyFriend;
    private Boolean friendRequestAlreadySent;
    private Boolean alreadyReceivedFriendRequest;
    private Boolean personalData;
}
