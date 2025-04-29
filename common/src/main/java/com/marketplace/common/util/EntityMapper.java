package com.marketplace.common.util;

public interface EntityMapper<T, R> {

    R mapEntityToDto(T entity);

    T mapDtoToEntity(R dto);

}
