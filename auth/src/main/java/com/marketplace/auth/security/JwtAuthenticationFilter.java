package com.marketplace.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                String subject = jwtService.extractSubject(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
                boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JWT_AUTHENTICATION_FILTER]: Token validated successfully");
                } else {
                    log.error("[JWT_AUTHENTICATION_FILTER]: Token validation failed");
                }

            } catch (SignatureException | MalformedJwtException | ExpiredJwtException exception) {
                log.error("[JWT_AUTHENTICATION_FILTER]: {}", exception.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if(bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return bearerToken.substring(BEARER_PREFIX.length());
    }
}
