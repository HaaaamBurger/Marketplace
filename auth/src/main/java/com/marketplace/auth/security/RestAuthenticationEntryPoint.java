package com.marketplace.auth.security;

import com.marketplace.auth.service.JwtCookieManager;
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

    private final JwtCookieManager jwtCookieManager;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("[REST_AUTHENTICATION_ENTRY_POINT]: {}", authException.getMessage());

        jwtCookieManager.deleteTokensFromCookie(response);

        response.sendRedirect("/sign-in?error=true");
    }
}
