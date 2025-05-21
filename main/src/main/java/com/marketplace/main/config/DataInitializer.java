package com.marketplace.main.config;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createUser(User.builder()
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("adminPassword1"))
                .status(UserStatus.ACTIVE)
                .role(UserRole.ADMIN)
                .build());
    }

    private void createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            log.info("[DATA_INITIALIZER]: Admin is already exist. Skipping creation.");
            return;
        }

        userRepository.save(user);
        log.info("[DATA_INITIALIZER]: Admin has been created.");
    }
}
