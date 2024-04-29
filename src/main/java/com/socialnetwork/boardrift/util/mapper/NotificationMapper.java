package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.rest.model.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NotificationMapper {

    @Mapping(target = "recipientId", expression = "java(entity.getRecipient().getId())")
    public abstract NotificationDto entityToDto(NotificationEntity entity);
}
