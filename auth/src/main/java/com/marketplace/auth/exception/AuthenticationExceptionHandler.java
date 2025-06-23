package com.marketplace.auth.exception;

import com.marketplace.common.exception.CommonExceptionService;
import com.marketplace.common.exception.ExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class AuthenticationExceptionHandler {

    private final CommonExceptionService commonExceptionService;

    @ExceptionHandler(CredentialException.class)
    public ModelAndView handleCredentialsException(CredentialException exception, HttpServletResponse response, HttpServletRequest request) {
        log.error("[AUTHENTICATION_EXCEPTION_HANDLER]: {}", exception.getMessage());

        response.setStatus(401);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                        .modelView("error")
                        .status(401)
                        .message(exception.getMessage())
                        .exceptionType(ExceptionType.AUTHORIZATION)
                        .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public void handleExpiredJwtException(ExpiredJwtException exception, HttpServletResponse response) throws IOException {
        log.error("[EXPIRED_JWT_EXCEPTION_HANDLER]: {}", exception.getMessage());
        response.sendRedirect("/sign-in");
    }

}
