package com.marketplace.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleTokenNotValidException(TokenNotValidException exception, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.AUTHORIZATION)
                        .path(request.getRequestURI())
                        .message(exception.getMessage())
                        .build()
        );
    }
}
