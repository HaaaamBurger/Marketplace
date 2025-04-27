package com.marketplace.main.util;

import com.marketplace.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.marketplace.auth.security.JwtService.BEARER_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthHelper {

    private static final String ROLES_CLAIM = "roles";

    private final JwtService jwtService;

    public String createAuth(UserDetails userDetails) {
        List<String> roles = getRoles(userDetails);

        String accessToken = jwtService.generateAccessToken(userDetails, Map.of(ROLES_CLAIM, roles));

        return BEARER_PREFIX + accessToken;

    }

    private List<String> getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

}
