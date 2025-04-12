package com.marketplace.product.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseExceptionHandler {

    protected ResponseEntity<ExceptionResponse> buildResponse(
            HttpStatus status,
            String message,
            ExceptionType type,
            HttpServletRequest request
    ) {
        ExceptionResponse response = ExceptionResponse.builder()
                .status(status.value())
                .type(type)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }
}

