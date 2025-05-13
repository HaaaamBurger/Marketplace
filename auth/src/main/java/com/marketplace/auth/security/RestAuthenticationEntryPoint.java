package com.marketplace.auth.security;

import com.marketplace.auth.security.cookie.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CookieService cookieService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("[REST_AUTHENTICATION_ENTRY_POINT]: {}", authException.getMessage());

        cookieService.deleteCookieByName(COOKIE_ACCESS_TOKEN, response);
        cookieService.deleteCookieByName(COOKIE_REFRESH_TOKEN, response);

        response.sendRedirect("/sign-in?error=true");
    }
}
