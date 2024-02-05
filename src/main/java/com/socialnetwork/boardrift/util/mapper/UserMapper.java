package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserEntity registrationDtoToEntity(UserRegistrationDto userRegistrationDto);
    UserRetrievalDto entityToRetrievalDto(UserEntity userEntity);
    UserRetrievalMinimalDto entityToMinimalRetrievalDto(UserEntity userEntity);
}
