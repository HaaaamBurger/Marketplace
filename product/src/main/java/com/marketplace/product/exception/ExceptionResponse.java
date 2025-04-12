package com.marketplace.product.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponse {
    private int status;
    private ExceptionType type;
    private String message;
    private String path;
}