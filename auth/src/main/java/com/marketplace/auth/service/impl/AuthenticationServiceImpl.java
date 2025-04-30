package com.marketplace.auth.service.impl;

import com.marketplace.auth.exception.CredentialException;
import com.marketplace.auth.exception.EntityExistsException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import com.marketplace.common.model.UserStatus;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse signIn(AuthRequest authRequest) {

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() ->  {
                    logAuthenticationError("User does not exist with email: " + authRequest.getEmail());
                    return new CredentialException("Wrong credentials!");
                });

        boolean isPasswordValid = passwordEncoder.matches(
                authRequest.getPassword(),
                user.getPassword()
        );

        if (!isPasswordValid) {
            logAuthenticationError("User password " + authRequest.getPassword() + " is not matching");
            throw new CredentialException("Wrong credentials!");
        }

        return generateTokenPair(user);
    }

    @Override
    public String signUp(AuthRequest authRequest) {

        throwExceptionIfUserExistsByEmail(authRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        userRepository.save(User.builder()
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .email(authRequest.getEmail())
                .password(encodedPassword)
                .build());

        return "User successfully created!";
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest) {
        String authRefreshToken = authRefreshRequest.getRefreshToken();

        try {
            UserDetails userDetails = getUserDetailsIfTokenValidOrThrowException(authRefreshToken);
            return generateTokenPair(userDetails);

        } catch (JwtException exception) {
            logAuthenticationError(exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }
    }

    private AuthResponse generateTokenPair(UserDetails userDetails) {

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private UserDetails getUserDetailsIfTokenValidOrThrowException(String token) {
        String subject = jwtService.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
            return userDetails;
        }
        throw new TokenNotValidException("Token not valid!");
    }

    private void throwExceptionIfUserExistsByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            logAuthenticationError("User by email " + email + " already exists!");
            throw new EntityExistsException("User already exists!");
        }
    }

    private void logAuthenticationError(String message) {
        log.error("[AUTHENTICATION_SERVICE_IMPL]: {}", message);
    }
}
