package com.marketplace.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.CredentialException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MainExceptionHandler {

    private static final String COMA_DELIMITER = ", ";
    private static final String SEMICOLON_DELIMITER = ": ";

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {

        String invalidFields = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(COMA_DELIMITER));

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.WEB)
                        .path(request.getRequestURI())
                        .message(invalidFields)
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

    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<ExceptionResponse> handleCredentialsException(CredentialException exception, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(401).value())
                        .type(ExceptionType.AUTHORIZATION)
                        .path(request.getRequestURI())
                        .message(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {

        String invalidFields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + SEMICOLON_DELIMITER + fieldError.getDefaultMessage())
                .collect(Collectors.joining(COMA_DELIMITER));

        return ResponseEntity.badRequest().body(
                ExceptionResponse.builder()
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.WEB)
                        .path(request.getRequestURI())
                        .message(invalidFields)
                        .build()
        );
    }
}
