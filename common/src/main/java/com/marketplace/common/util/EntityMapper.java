package com.marketplace.common.util;

public interface EntityMapper<Entity, RequestDto, ResponseDto> {

    ResponseDto mapEntityToResponseDto(Entity entity);

    Entity mapRequestDtoToEntity(RequestDto requestDto);

}
