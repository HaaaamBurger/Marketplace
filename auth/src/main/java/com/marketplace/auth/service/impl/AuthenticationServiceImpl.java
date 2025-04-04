package com.marketplace.auth.service.impl;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public AuthResponse signIn(AuthRequest authRequest) {

        User user = (User) userDetailsService.loadUserByUsername(authRequest.getEmail());

        System.out.println("User: " + user);

        return AuthResponse.builder()
                .accessToken(String.valueOf(UUID.randomUUID()))
                .refreshToken(String.valueOf(UUID.randomUUID()))
                .build();
    }

    @Override
    public String signUp(AuthRequest authRequest) {

        Optional<User> byEmail = userRepository.findByEmail(authRequest.getEmail());

        if (byEmail.isEmpty()) {
            throw new EntityNotFoundException("User not found!!");
        }

        User savedUser = userRepository.save(User.builder()
                .role(UserRole.USER)
                .email(authRequest.getPassword())
                .password(authRequest.getPassword())
                .build());

        System.out.println("New user: " + savedUser);

        return "User successfully created!!";
    }
}
