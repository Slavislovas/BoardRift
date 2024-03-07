package com.socialnetwork.boardrift.rest.model;

import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {
    private UserRetrievalMinimalDto sender;
    private UserRetrievalMinimalDto receiver;
}
