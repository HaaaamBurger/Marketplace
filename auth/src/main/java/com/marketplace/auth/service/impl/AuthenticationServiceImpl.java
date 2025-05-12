package com.marketplace.auth.service.impl;

import com.marketplace.auth.exception.CredentialException;
import com.marketplace.auth.exception.EntityExistsException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.cookie.CookiePayload;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.service.JwtService;
import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import com.marketplace.auth.web.model.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userDetailsService;

    private final CookieService cookieService;

    @Override
    public AuthResponse signIn(AuthRequest authRequest, HttpServletResponse response) {

        User user = findUserByEmailOrThrow(authRequest.getEmail());
        boolean isPasswordValid = passwordEncoder.matches(
                authRequest.getPassword(),
                user.getPassword()
        );

        if (!isPasswordValid) {
            log.error("[AUTHENTICATION_SERVICE_IMPL]: User password {} is not matching", authRequest.getPassword());
            throw new CredentialException("Wrong credentials!");
        }

        AuthResponse authResponse = generateAuthResponse(user);
        addTokensToCookie(authResponse, response);

        return authResponse;
    }

    @Override
    public void signUp(AuthRequest authRequest) {

        throwExceptionIfUserExistsByEmail(authRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        userRepository.save(User.builder()
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .email(authRequest.getEmail())
                .password(encodedPassword)
                .build());
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest) {
        String authRefreshToken = authRefreshRequest.getRefreshToken();

        try {
            UserDetails userDetails = getUserDetailsIfTokenValidOrThrowException(authRefreshToken);
            return generateAuthResponse(userDetails);

        } catch (JwtException exception) {
            log.error("[AUTHENTICATION_SERVICE_IMPL]: {}", exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }
    }

    private void addTokensToCookie(AuthResponse authResponse, HttpServletResponse httpServletResponse) {
        CookiePayload accessTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_ACCESS_TOKEN)
                .value(authResponse.getAccessToken())
                .maxAge(jwtService.JWT_ACCESS_EXPIRATION_TIME)
                .build();
        CookiePayload refreshTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_REFRESH_TOKEN)
                .value(authResponse.getRefreshToken())
                .maxAge(jwtService.JWT_REFRESH_EXPIRATION_TIME)
                .build();

        cookieService.addValueToCookie(accessTokenCookiePayload, httpServletResponse);
        cookieService.addValueToCookie(refreshTokenCookiePayload, httpServletResponse);
    }

    private AuthResponse generateAuthResponse(UserDetails userDetails) {

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
            log.error("[AUTHENTICATION_SERVICE_IMPL]: User by email {} already exists!", email);
            throw new EntityExistsException("User already exists!");
        }
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->  {
                    log.error("[AUTHENTICATION_SERVICE_IMPL]:User does not exist with email: {}", email);
                    return new CredentialException("Wrong credentials!");
                });
    }
}
