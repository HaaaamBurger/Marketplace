package com.marketplace.common.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public abstract class BaseResponse {

    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
