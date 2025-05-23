package com.marketplace.main.exception;

import com.marketplace.auth.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import static com.marketplace.common.constants.Delimiters.COMMA_DELIMITER;
import static com.marketplace.common.constants.Delimiters.COLON_DELIMITER;

@RestControllerAdvice
public class MainExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {

        String invalidFields = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(COMMA_DELIMITER));

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
                        .status(HttpStatusCode.valueOf(400).value())
                        .type(ExceptionType.WEB)
                        .path(request.getRequestURI())
                        .message(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFoundException(EntityNotFoundException exception, HttpServletRequest request) {

        return ResponseEntity.status(404).body(
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

        return ResponseEntity.status(401).body(
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
                .map(fieldError -> fieldError.getField() + COLON_DELIMITER + fieldError.getDefaultMessage())
                .collect(Collectors.joining(COMMA_DELIMITER));

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
