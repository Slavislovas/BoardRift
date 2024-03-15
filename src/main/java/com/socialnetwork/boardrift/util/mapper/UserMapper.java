package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.AWSService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {
    @Autowired
    AWSService awsService;

   public abstract UserEntity registrationDtoToEntity(UserRegistrationDto userRegistrationDto);

    @Mapping(target = "userAlreadyFriend", expression = "java(userAlreadyFriend)")
    @Mapping(target = "friendRequestAlreadySent", expression = "java(friendRequestAlreadySent)")
    @Mapping(target = "alreadyReceivedFriendRequest", expression = "java(alreadyReceivedFriendRequest)")
    @Mapping(target = "personalData", expression = "java(personalData)")
    @Mapping(source = "userEntity", target = "profilePictureUrl", qualifiedByName = "toSignedProfilePictureUrl")
    public abstract UserRetrievalDto entityToRetrievalDto(UserEntity userEntity,
                                                          Boolean userAlreadyFriend,
                                                          Boolean friendRequestAlreadySent,
                                                          Boolean alreadyReceivedFriendRequest,
                                                          Boolean personalData);

    @Mapping(source = "userEntity", target = "profilePictureUrl", qualifiedByName = "toSignedProfilePictureUrl")
    public abstract UserRetrievalMinimalDto entityToMinimalRetrievalDto(UserEntity userEntity);

    @Named("toSignedProfilePictureUrl")
    public String toSignedProfilePictureUrl(UserEntity userEntity) {
        return awsService.getPreSignedUrl(userEntity.getId());
    }
}
