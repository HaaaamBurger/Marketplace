package com.marketplace.auth.util;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
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

    private final UserRepository userRepository;


    public String createUserAuth() {
        User user = User.builder()
                .email("testuser@gmail.com")
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        return createAuth(user);
    }

    public String createAdminAuth() {
        User user = User.builder()
                .email("testadmin@gmail.com")
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(user);

        return createAuth(user);
    }

    private String createAuth(UserDetails userDetails) {
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
