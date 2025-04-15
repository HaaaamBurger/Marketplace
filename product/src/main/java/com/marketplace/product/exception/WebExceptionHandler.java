package com.marketplace.product.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class WebExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request) {

        String constraintViolations = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .type(ExceptionType.WEB)
                        .message(constraintViolations)
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exception, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .type(ExceptionType.WEB)
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleProductNotFoundException(
            ProductNotFoundException exception, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .type(ExceptionType.WEB)
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build()
                );
    }
}
