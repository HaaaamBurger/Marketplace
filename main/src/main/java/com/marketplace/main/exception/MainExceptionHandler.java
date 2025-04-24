package com.marketplace.main.exception;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.ExceptionResponse;
import com.marketplace.common.exception.ExceptionType;
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
                .map(fieldError -> fieldError.getField() + COLON_DELIMITER + fieldError.getDefaultMessage())
                .collect(Collectors.joining(COLON_DELIMITER));

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
