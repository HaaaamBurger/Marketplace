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
        String email = "userauth@gmail.com";
        return createUserAuth(email);
    }

    public AuthHelperResponse createUserAuth(String email) {
        User user = User.builder()
                .email(email)
                .role(UserRole.USER)
                .build();

        User authUser = userRepository.save(user);

        return AuthHelperResponse.builder()
                .token(createAuth(authUser))
                .authUser(authUser)
                .build();
    }

    public AuthHelperResponse createAdminAuth() {
        String email = "adminauth@gmail.com";
        return createAdminAuth(email);
    }

    public AuthHelperResponse createAdminAuth(String email) {
        User user = User.builder()
                .email(email)
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
