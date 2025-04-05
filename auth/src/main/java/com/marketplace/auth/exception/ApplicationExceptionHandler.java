package com.marketplace.auth.exception;

import com.marketplace.auth.common.ExceptionType;
import com.marketplace.auth.web.rest.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {

    // example of future application exceptions handling
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ExceptionResponse> handleIOException(IOException exception, HttpServletRequest request) {

        log.error("[IOException]: {}", exception.getMessage());

        return ResponseEntity.badRequest()
                .body(ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.APPLICATION)
                        .path(request.getRequestURI())
                        .message("Application exception has occurred.")
                        .build());
    }
}
