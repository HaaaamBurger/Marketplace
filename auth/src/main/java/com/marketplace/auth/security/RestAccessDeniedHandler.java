package com.marketplace.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.common.exception.ExceptionResponse;
import com.marketplace.common.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.error("[REST_ACCESS_DENIED_HANDLER]: {}", accessDeniedException.getMessage());

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .type(ExceptionType.AUTHORIZATION)
                .path(request.getRequestURI())
                .message("Forbidden, not enough access!")
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
    }

}
