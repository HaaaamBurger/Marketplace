package com.marketplace.common.mapper;

public interface EntityMapper<Entity, RequestDto, ResponseDto> {

    ResponseDto mapEntityToResponseDto(Entity entity);

    Entity mapRequestDtoToEntity(RequestDto requestDto);

}
