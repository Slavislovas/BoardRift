package com.socialnetwork.boardrift.util.mapper;

import com.socialnetwork.boardrift.rest.model.BoardGameDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardGameMapper {
//    BoardGameDto entityToDto(BoardGameEntity entity);
}
