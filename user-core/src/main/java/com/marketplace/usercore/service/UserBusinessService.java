package com.marketplace.usercore.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBusinessService implements UserManagerService {

    private final UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void throwIfUserExistsByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            log.error("[USER_SERVICE_FACADE]: User already exists by email: {}", email);
            throw new EntityExistsException("User already exists!");
        });

    }

    @Override
    public User throwIfUserNotFoundById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[USER_SERVICE_FACADE]: User not found by id: {}", userId);
                    return new EntityNotFoundException("User not found!");
                });
    }

    @Override
    public void throwIfUserWithSameEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EntityExistsException("User with this email already exists");
        }
    }

}
