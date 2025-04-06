package com.marketplace.auth.service.impl;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse signIn(AuthRequest authRequest) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String signUp(AuthRequest authRequest) {

        Optional<User> byEmail = userRepository.findByEmail(authRequest.getEmail());

        if (byEmail.isPresent()) {
            throw new EntityExistsException("User already exists!");
        }

        userRepository.save(User.builder()
                .role(UserRole.USER)
                .email(authRequest.getEmail())
                .password(authRequest.getPassword())
                .build());

        return "User successfully created!!";
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest) {
        String authRefreshToken = authRefreshRequest.getRefreshToken();
        String subject = jwtService.extractSubject(authRefreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

        try {
            boolean isTokenValid = jwtService.isTokenValid(authRefreshRequest.getRefreshToken(), userDetails);

            if(isTokenValid) {
                String accessToken = jwtService.generateAccessToken(userDetails);
                String refreshToken = jwtService.generateRefreshToken(userDetails);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException exception) {
            log.error("[AUTHENTICATION_SERVICE]: {}", exception.getMessage());
        }

        return null;
    }
}
