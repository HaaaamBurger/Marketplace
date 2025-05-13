package com.marketplace.auth.security;

import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.service.CustomUserDetailsService;
import com.marketplace.auth.service.JwtCookieManager;
import com.marketplace.auth.service.JwtTokenManager;
import com.marketplace.auth.web.dto.TokenPayload;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.marketplace.auth.security.config.SecurityConfig.PERMITTED_ROUTES;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    private final CookieService cookieService;

    private final JwtCookieManager jwtCookieManager;

    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (validatePermittedRoute(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessTokenCookie = cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, request);
        if (isAuthenticatedOrNoCookie(accessTokenCookie)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = accessTokenCookie.getValue();
        try {
            addAuthenticationToContext(accessToken);
        } catch (JwtException exception) {

            boolean refreshValid = updateTokensIfRefreshValid(response, request);

            if (refreshValid) {
                filterChain.doFilter(request, response);
                return;
            }

            log.error("[JWT_AUTHENTICATION_FILTER]: {}", exception.getMessage());
            jwtCookieManager.deleteTokensFromCookie(response);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("[JWT_AUTHENTICATION_FILTER]: Token validated successfully");
        filterChain.doFilter(request, response);
    }

    private boolean validatePermittedRoute(String requestRoute) {
        return Arrays.stream(PERMITTED_ROUTES).anyMatch(permittedRoute -> permittedRoute.contains(requestRoute));
    }

    private boolean isAuthenticatedOrNoCookie(Cookie cookie) {
        if (cookie == null) {
            return true;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext.getAuthentication() != null || cookie.getValue() == null;
    }

    private UserDetails addAuthenticationToContext(String token) {
        UserDetails userDetails = jwtTokenManager.getUserDetailsIfTokenValidOrThrow(token);
        customUserDetailsService.validateUserNotBlockedOrThrow(userDetails);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return userDetails;
    }

    private boolean updateTokensIfRefreshValid(HttpServletResponse response, HttpServletRequest request) {
        try {
            Cookie refreshTokenCookie = cookieService.extractCookieByName(COOKIE_REFRESH_TOKEN, request);

            if (refreshTokenCookie == null) {
                return false;
            }

            UserDetails userDetails = jwtTokenManager.getUserDetailsIfTokenValidOrThrow(refreshTokenCookie.getValue());
            TokenPayload tokenPayload = jwtTokenManager.generateTokenPayload(userDetails);
            jwtCookieManager.addTokensToCookie(tokenPayload, response);

            log.info("[JWT_AUTHENTICATION_FILTER]: Tokens refreshed successfully");

            return true;
        } catch (JwtException exception) {
            return false;
        }
    }
}
