package com.marketplace.auth.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public void handleExpiredJwtException(ExpiredJwtException exception, HttpServletResponse response) throws IOException {
        log.error("[EXPIRED_JWT_EXCEPTION_HANDLER]: {}", exception.getMessage());
        response.sendRedirect("/sign-in");
    }
}
