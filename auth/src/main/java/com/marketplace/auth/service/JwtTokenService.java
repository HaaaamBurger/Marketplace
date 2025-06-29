package com.marketplace.auth.service;

import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.security.token.JwtService;
import com.marketplace.auth.security.token.TokenPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    public TokenPayload generateTokenPayload(UserDetails userDetails) {

        if (userDetails == null) {
            throw new IllegalArgumentException("User details not present");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return TokenPayload.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public UserDetails getUserDetailsIfTokenValidOrThrow(String token) {
        String subject = jwtService.extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        boolean isTokenValid = jwtService.isTokenValid(token, userDetails);

        if (isTokenValid) {
            return userDetails;
        }

        throw new TokenNotValidException("Token not valid!");
    }


}
