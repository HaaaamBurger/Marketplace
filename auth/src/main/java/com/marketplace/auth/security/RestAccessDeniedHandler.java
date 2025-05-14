package com.marketplace.auth.security;

import com.marketplace.auth.service.JwtCookieManager;
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

    private final JwtCookieManager jwtCookieManager;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.error("[REST_ACCESS_DENIED_HANDLER]: {}", accessDeniedException.getMessage());
        jwtCookieManager.deleteTokensFromCookie(response);
        response.sendRedirect("/sign-in?error=true");
    }

}
