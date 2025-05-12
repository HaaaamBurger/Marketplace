package com.marketplace.auth.security;

import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.security.cookie.CookiePayload;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.service.CustomUserDetailsService;
import com.marketplace.auth.security.service.JwtService;
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

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

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
            deleteTokensFromCookie(response);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("[JWT_AUTHENTICATION_FILTER]: Token validated successfully");
        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedOrNoCookie(Cookie cookie) {
        if (cookie == null) {
            return true;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext.getAuthentication() != null || cookie.getValue() == null;
    }

    private UserDetails addAuthenticationToContext(String token) {
        UserDetails userDetails = getUserDetailsIfTokenValidOrThrow(token);
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

            UserDetails userDetails = getUserDetailsIfTokenValidOrThrow(refreshTokenCookie.getValue());

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            addTokensToCookie(accessToken, refreshToken, response);

            log.info("[JWT_AUTHENTICATION_FILTER]: Tokens refreshed successfully");

            return true;
        } catch (JwtException exception) {
            return false;
        }

    }

    private UserDetails getUserDetailsIfTokenValidOrThrow(String token) {

        String subject = jwtService.extractSubject(token);
        UserDetails userDetails =  customUserDetailsService.loadUserByUsername(subject);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
            return userDetails;
        }

        log.error("[JWT_AUTHENTICATION_FILTER]: Token validation failed");
        throw new TokenNotValidException("Token not valid!");

    }

    private void addTokensToCookie(String accessToken, String refreshToken, HttpServletResponse response) {

        CookiePayload accessTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_ACCESS_TOKEN)
                .value(accessToken)
                .maxAge(jwtService.JWT_ACCESS_EXPIRATION_TIME)
                .build();

        CookiePayload refreshTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_REFRESH_TOKEN)
                .value(refreshToken)
                .maxAge(jwtService.JWT_REFRESH_EXPIRATION_TIME)
                .build();

        cookieService.addValueToCookie(accessTokenCookiePayload, response);
        cookieService.addValueToCookie(refreshTokenCookiePayload, response);

    }

    private void deleteTokensFromCookie(HttpServletResponse response) {
        cookieService.deleteCookieByName(COOKIE_ACCESS_TOKEN, response);
        cookieService.deleteCookieByName(COOKIE_REFRESH_TOKEN, response);
    }

}
