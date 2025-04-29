package com.marketplace.auth.util;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import static com.marketplace.auth.security.JwtService.BEARER_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthHelper {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    public AuthHelperResponse createUserAuth() {
        User user = User.builder()
                .email("testuser@gmail.com")
                .role(UserRole.USER)
                .build();

        User authUser = userRepository.save(user);

        return AuthHelperResponse.builder()
                .token(createAuth(authUser))
                .authUser(authUser)
                .build();
    }

    public AuthHelperResponse createAdminAuth() {
        User user = User.builder()
                .email("testadmin@gmail.com")
                .role(UserRole.ADMIN)
                .build();

        User authUser = userRepository.save(user);

        return AuthHelperResponse.builder()
                .token(createAuth(authUser))
                .authUser(authUser)
                .build();
    }

    private String createAuth(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        return BEARER_PREFIX + accessToken;
    }

    @Data
    @Builder
    public static class AuthHelperResponse {

        private String token;

        private User authUser;

    }

}
