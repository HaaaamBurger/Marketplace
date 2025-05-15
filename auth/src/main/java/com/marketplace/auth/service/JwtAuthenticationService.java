package com.marketplace.auth.service;

import com.marketplace.auth.exception.CredentialException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.web.dto.TokenPayload;
import com.marketplace.auth.web.dto.AuthRefreshRequest;
import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.auth.web.dto.AuthResponse;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.service.MongoUserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MongoUserService mongoUserService;

    private final JwtTokenManager jwtTokenManager;

    private final JwtCookieManager jwtCookieManager;

    @Override
    public AuthResponse signIn(AuthRequest authRequest, HttpServletResponse response) {

        User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow(() ->  {
                    log.error("[JWT_AUTHENTICATION_SERVICE]:User does not exist with email: {}", authRequest.getEmail());
                    return new CredentialException("Wrong credentials!");
                });

        boolean isPasswordValid = passwordEncoder.matches(
                authRequest.getPassword(),
                user.getPassword()
        );

        if (!isPasswordValid) {
            log.error("[JWT_AUTHENTICATION_SERVICE]: User password {} is not matching", authRequest.getPassword());
            throw new CredentialException("Wrong credentials!");
        }

        TokenPayload tokenPayload = jwtTokenManager.generateTokenPayload(user);
        jwtCookieManager.addTokensToCookie(tokenPayload, response);

        return AuthResponse.builder()
                .accessToken(tokenPayload.getAccessToken())
                .refreshToken(tokenPayload.getRefreshToken())
                .build();
    }

    @Override
    public void signUp(AuthRequest authRequest) {

        mongoUserService.throwIfUserExistsByEmail(authRequest.getEmail());
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
            UserDetails userDetails = jwtTokenManager.getUserDetailsIfTokenValidOrThrow(authRefreshToken);
            TokenPayload tokenPayload = jwtTokenManager.generateTokenPayload(userDetails);

            return AuthResponse.builder()
                    .accessToken(tokenPayload.getAccessToken())
                    .refreshToken(tokenPayload.getRefreshToken())
                    .build();

        } catch (JwtException exception) {
            log.error("[JWT_AUTHENTICATION_SERVICE]: {}", exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }
    }
}
