package com.marketplace.auth.security;

import com.marketplace.auth.security.cookie.CookieNotFoundException;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.token.TokenPayload;
import com.marketplace.auth.service.JwtCookieService;
import com.marketplace.auth.service.JwtTokenService;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CookieService cookieService;

    private final JwtCookieService jwtCookieService;

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            if (securityContext.getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            Cookie accessTokenCookie = cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, request);
            UserDetails userDetails = validateUserAccessibility(accessTokenCookie.getValue());
            addAuthenticationToContext(userDetails);

        } catch (JwtException exception) {
            log.error("[JWT_AUTHENTICATION_FILTER]: {}", exception.getMessage());

            boolean refreshValid = updateTokensIfRefreshValid(response, request);
            if (refreshValid) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtCookieService.deleteTokensFromCookie(response);
            response.sendRedirect("/sign-in");
            return;
        } catch (UsernameNotFoundException | AccessDeniedException exception) {
            log.error("[JWT_AUTHENTICATION_FILTER]: {}", exception.getMessage());

            jwtCookieService.deleteTokensFromCookie(response);
            return;
        } catch (CookieNotFoundException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void addAuthenticationToContext(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private UserDetails validateUserAccessibility(String token) {
        UserDetails userDetails = jwtTokenService.getUserDetailsIfTokenValidOrThrow(token);

        if (userDetails instanceof User && ((User) userDetails).getStatus() == UserStatus.BLOCKED) {
            throw new AccessDeniedException("User is blocked");
        }

        return userDetails;
    }

    private boolean updateTokensIfRefreshValid(HttpServletResponse response, HttpServletRequest request) {
        try {
            Cookie refreshTokenCookie = cookieService.extractCookieByName(COOKIE_REFRESH_TOKEN, request);

            UserDetails userDetails = jwtTokenService.getUserDetailsIfTokenValidOrThrow(refreshTokenCookie.getValue());
            TokenPayload tokenPayload = jwtTokenService.generateTokenPayload(userDetails);
            jwtCookieService.addTokensToCookie(tokenPayload, response);

            log.info("[JWT_AUTHENTICATION_FILTER]: Tokens refreshed successfully");

            return true;
        } catch (JwtException exception) {
            log.info("[JWT_AUTHENTICATION_FILTER]: Tokens refresh failed {}", exception.getMessage());

            return false;
        }
    }
}
