package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.NotificationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {
   public abstract UserEntity registrationDtoToEntity(UserRegistrationDto userRegistrationDto);

    @Mapping(target = "userAlreadyFriend", expression = "java(userAlreadyFriend)")
    @Mapping(target = "friendRequestAlreadySent", expression = "java(friendRequestAlreadySent)")
    @Mapping(target = "alreadyReceivedFriendRequest", expression = "java(alreadyReceivedFriendRequest)")
    @Mapping(target = "personalData", expression = "java(personalData)")
    @Mapping(target = "suspended", expression = "java(!userEntity.isEnabled())")
    public abstract UserRetrievalDto entityToRetrievalDto(UserEntity userEntity,
                                                          Boolean userAlreadyFriend,
                                                          Boolean friendRequestAlreadySent,
                                                          Boolean alreadyReceivedFriendRequest,
                                                          Boolean personalData);

    @Mapping(target = "suspended", expression = "java(!userEntity.isEnabled())")
    @Mapping(target = "warningCount", expression = "java(userEntity.getReceivedWarnings().size())")
    public abstract UserRetrievalMinimalDto entityToMinimalRetrievalDto(UserEntity userEntity);
}
