package com.marketplace.product.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.exception.ExceptionResponse;
import com.marketplace.product.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("[REST_AUTHENTICATION_ENTRY_POINT]: {}", authException.getMessage());

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .type(ExceptionType.AUTHORIZATION)
                .path(request.getRequestURI())
                .message("Authentication required, please sign in")
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));

    }
}
