package com.marketplace.auth.service;

import com.marketplace.auth.exception.EntityExistsException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
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
    @SneakyThrows
    public AuthResponse signIn(AuthRequest authRequest) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

        throwExceptionIfPasswordsNotMatching(authRequest.getPassword(), userDetails.getPassword());

        return generateTokenPair(userDetails);
    }

    @Override
    public String signUp(AuthRequest authRequest) {

        throwExceptionIfUserExistsByEmail(authRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        userRepository.save(User.builder()
                .role(UserRole.USER)
                .email(authRequest.getEmail())
                .password(encodedPassword)
                .build());

        return "User successfully created!";
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest) {
        String authRefreshToken = authRefreshRequest.getRefreshToken();

        try {
            UserDetails userDetails = getUserDetailsIfTokenValid(authRefreshToken);

            if (userDetails != null) {
                return generateTokenPair(userDetails);
            }

            throw new TokenNotValidException("Token not valid!");

        } catch (JwtException exception) {
            log.error("[AUTHENTICATION_SERVICE]: {}", exception.getMessage());
            throw new TokenNotValidException("Token not valid!");
        }
    }

    private void throwExceptionIfPasswordsNotMatching(String rawPassword, String encodedPassword) throws CredentialException {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        if (!matches) {
            throw new CredentialException("Wrong credentials!");
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

    private UserDetails getUserDetailsIfTokenValid(String token) {
        String subject = jwtService.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
            return userDetails;
        }

        return null;
    }

    private void throwExceptionIfUserExistsByEmail(String email) {
        Optional<User> byEmail = userRepository.findByEmail(email);

        if (byEmail.isPresent()) {
            throw new EntityExistsException("User already exists!");
        }
    }
}
