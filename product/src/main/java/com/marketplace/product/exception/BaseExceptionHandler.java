package com.marketplace.product.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseExceptionHandler {

    protected ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", String.valueOf(status.value()));
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return new ResponseEntity<>(body, status);
    }
}
