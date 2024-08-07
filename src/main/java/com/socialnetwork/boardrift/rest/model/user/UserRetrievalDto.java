package com.socialnetwork.boardrift.rest.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.socialnetwork.boardrift.rest.model.WarningDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String profilePictureUrl;
    private String bio;
    private String country;
    private String city;
    private Boolean publicPosts;
    private Boolean publicFriendsList;
    private Boolean publicPlays;
    private Boolean publicStatistics;
    private Boolean userAlreadyFriend;
    private Boolean friendRequestAlreadySent;
    private Boolean alreadyReceivedFriendRequest;
    private Boolean personalData;
    private Boolean suspended;
    private List<WarningDto> receivedWarnings;
}
