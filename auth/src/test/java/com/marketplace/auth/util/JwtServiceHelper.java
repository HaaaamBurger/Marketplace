package com.marketplace.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceHelper {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    public String generateAccessTokenWithExpiration(UserDetails userDetails, int expiration) {
        return buildToken(userDetails, new HashMap<>(), expiration);
    }

    public String generateRefreshTokenWithExpiration(UserDetails userDetails, int expiration) {
        return buildToken(userDetails, new HashMap<>(), expiration);
    }

    private String buildToken(UserDetails userDetails, Map<String, Object> claims, int expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

}
