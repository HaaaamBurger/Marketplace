package com.marketplace.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.access-expiration-time}")
    private long jwtAccessExpirationTime;

    @Value("${security.jwt.refresh-expiration-time}")
    private long jwtRefreshExpirationTime;

    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails, new HashMap<>());
    }

    public String generateAccessToken(UserDetails userDetails, Map<String, Object> claims) {
        return buildToken(userDetails, claims, jwtAccessExpirationTime);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(userDetails, new HashMap<>());
    }

    public String generateRefreshToken(UserDetails userDetails, Map<String, Object> claims) {
        return buildToken(userDetails, claims, jwtRefreshExpirationTime);
    }

    public String generateRefreshTokenWithExpiration(UserDetails userDetails, long expiration) {
        return buildToken(userDetails, new HashMap<>(), expiration);
    }

    private String buildToken(UserDetails userDetails, Map<String, Object> claims, long expiration) {
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String subject = extractClaim(token, Claims::getSubject);

        return (!isTokenExpired(token) && userDetails.getUsername().equals(subject));
    }

    protected boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException exception) {
            return true;
        } catch (JwtException exception) {
            return false;
        }
    }

    private  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}