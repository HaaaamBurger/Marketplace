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
import com.marketplace.usercore.service.UserManagerService;
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
public class AuthenticationBusinessManagerService implements AuthenticationManagerService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserManagerService userManagerService;

    private final JwtTokenService jwtTokenService;

    private final JwtCookieService jwtCookieService;

    @Override
    public AuthResponse signIn(AuthRequest authRequest, HttpServletResponse response) {

        User user = findUserByEmailOrThrow(authRequest.getEmail());

        matchPasswordsOrThrow(authRequest.getPassword(), user.getPassword());
        TokenPayload tokenPayload = jwtTokenService.generateTokenPayload(user);
        jwtCookieService.addTokensToCookie(tokenPayload, response);

        return AuthResponse.builder()
                .accessToken(tokenPayload.getAccessToken())
                .refreshToken(tokenPayload.getRefreshToken())
                .build();
    }

    @Override
    public void signUp(AuthRequest authRequest) {

        userManagerService.throwIfUserExistsByEmail(authRequest.getEmail());
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
            UserDetails userDetails = jwtTokenService.getUserDetailsIfTokenValidOrThrow(authRefreshToken);
            TokenPayload tokenPayload = jwtTokenService.generateTokenPayload(userDetails);

            return AuthResponse.builder()
                    .accessToken(tokenPayload.getAccessToken())
                    .refreshToken(tokenPayload.getRefreshToken())
                    .build();

        } catch (JwtException exception) {
            throw new TokenNotValidException("Token not valid!");
        }
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new CredentialException("Wrong credentials!"));
    }

    private void matchPasswordsOrThrow(String rawPassword, String encodedPassword) throws CredentialException {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CredentialException("Wrong credentials!");
        }
    }

}
