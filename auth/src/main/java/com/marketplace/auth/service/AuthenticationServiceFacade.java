package com.marketplace.auth.service;

import com.marketplace.auth.exception.CredentialException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.security.token.TokenPayload;
import com.marketplace.auth.web.dto.AuthRefreshRequest;
import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.auth.web.dto.AuthResponse;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.service.UserServiceFacade;
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
public class AuthenticationServiceFacade implements AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserServiceFacade userServiceFacade;

    private final JwtTokenManager jwtTokenManager;

    private final JwtCookieManager jwtCookieManager;

    @Override
    public AuthResponse signIn(AuthRequest authRequest, HttpServletResponse response) {

        User user = findUserByEmailOrThrow(authRequest.getEmail());

        matchPasswordsOrThrow(authRequest.getPassword(), user.getPassword());
        TokenPayload tokenPayload = jwtTokenManager.generateTokenPayload(user);
        jwtCookieManager.addTokensToCookie(tokenPayload, response);

        return AuthResponse.builder()
                .accessToken(tokenPayload.getAccessToken())
                .refreshToken(tokenPayload.getRefreshToken())
                .build();
    }

    @Override
    public void signUp(AuthRequest authRequest) {

        userServiceFacade.throwIfUserExistsByEmail(authRequest.getEmail());
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
            log.error("[AUTHENTICATION_SERVICE_FACADE]: {}", exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->  {
            log.error("[AUTHENTICATION_SERVICE_FACADE]: User does not exist with email: {}",email);
            return new CredentialException("Wrong credentials!");
        });
    }

    private void matchPasswordsOrThrow(String rawPassword, String encodedPassword) throws CredentialException {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.error("[AUTHENTICATION_SERVICE_FACADE]: User password {} is not matching", rawPassword);
            throw new CredentialException("Wrong credentials!");
        }
    }

}
