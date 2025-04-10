package com.marketplace.auth.service;

import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityExistsException;
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

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @SneakyThrows
    public AuthResponse signIn(AuthRequest authRequest) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

        boolean matches = passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword());

        if(!matches) {
            throw new CredentialException("Wrong credentials!!");
        }

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

        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        userRepository.save(User.builder()
                .role(UserRole.USER)
                .email(authRequest.getEmail())
                .password(encodedPassword)
                .build());

        return "User successfully created!!";
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest) {
        String authRefreshToken = authRefreshRequest.getRefreshToken();

        try {
            String subject = jwtService.extractSubject(authRefreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

            boolean isTokenValid = jwtService.isTokenValid(authRefreshToken, userDetails);

            if (isTokenValid) {
                String accessToken = jwtService.generateAccessToken(userDetails);
                String refreshToken = jwtService.generateRefreshToken(userDetails);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }

            throw new TokenNotValidException("Refresh token not valid!!");

        } catch (JwtException exception) {
            log.error("[AUTHENTICATION_SERVICE]: {}", exception.getMessage());
            throw new TokenNotValidException(exception.getMessage());
        }
    }
}
