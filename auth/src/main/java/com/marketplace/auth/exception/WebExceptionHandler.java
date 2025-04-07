package com.marketplace.auth.exception;

import com.marketplace.auth.common.ExceptionType;
import com.marketplace.auth.web.rest.dto.ExceptionResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class WebExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {

        String constraintViolations = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","));

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.WEB)
                        .path(request.getRequestURI())
                        .message(constraintViolations)
                        .build()
        );
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ExceptionResponse> handleEntityExistsException(EntityExistsException exception, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(404).value())
                        .type(ExceptionType.WEB)
                        .path(request.getRequestURI())
                        .message(exception.getMessage())
                        .build()
        );
    }
}
