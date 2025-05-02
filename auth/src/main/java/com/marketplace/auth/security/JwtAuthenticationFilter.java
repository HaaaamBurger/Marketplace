package com.marketplace.auth.security;

import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.web.model.User;
import com.marketplace.common.model.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.marketplace.auth.security.JwtService.AUTHORIZATION_HEADER;
import static com.marketplace.auth.security.JwtService.BEARER_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (isAuthenticatedOrNoToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            addAuthenticationToContext(token);
        } catch (JwtException exception) {
            log.error("[JWT_AUTHENTICATION_FILTER]: {}", exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (isBearerTokenValid(bearerToken)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private boolean isBearerTokenValid(String bearerToken) {
        return bearerToken != null && bearerToken.startsWith(BEARER_PREFIX);
    }

    private boolean isAuthenticatedOrNoToken(String token) {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return securityContext.getAuthentication() != null || token == null;
    }

    private void addAuthenticationToContext(String token) {
        UserDetails userDetails = getUserDetailsIfTokenValid(token);

        validateUserNotBlocked((User) userDetails);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("[JWT_AUTHENTICATION_FILTER]: Token validated successfully");
    }

    private UserDetails getUserDetailsIfTokenValid(String token) {
        String subject = jwtService.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
            return userDetails;
        }

        log.error("[JWT_AUTHENTICATION_FILTER]: Token validation failed");
        throw new TokenNotValidException("Token not valid!");
    }

    private void validateUserNotBlocked(User user) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            log.error("[JWT_AUTHENTICATION_FILTER]: User {} cannot access this resource because status is {}", user.getId(), UserStatus.BLOCKED);
            throw new AccessDeniedException("Forbidden, not enough access!");
        }
    }
}
