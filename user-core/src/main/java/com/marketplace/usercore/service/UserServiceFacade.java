package com.marketplace.usercore.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.security.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.mapper.UserEntityMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class UserServiceFacade implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ProfileService profileService;

    private final UserEntityMapper userEntityMapper;

    @Override
    public User create(UserRequest userRequest) {
        throwIfUserExistsByEmail(userRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        User user = userEntityMapper.mapUserRequestDtoToUser(userRequest).toBuilder()
                .status(UserStatus.ACTIVE)
                .password(encodedPassword)
                .build();

        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(String userId) {
        return throwIfUserNotFoundById(userId);
    }

    @Override
    public User update(String userId, UserUpdateRequest userUpdateRequest) {
        User userForUpdate = throwIfUserNotFoundById(userId);
        User authenticatedUser = profileService.getAuthenticatedUser();

        if (!validateEntityOwnerOrAdmin(authenticatedUser, userId)) {
            log.error("[USER_SERVICE_FACADE]: User {} is not owner or not ADMIN", authenticatedUser.getId());
            throw new AccessDeniedException("Access denied!");
        }

        throwIfUserWithSameEmailExists(userUpdateRequest.getEmail());

        Optional.ofNullable(userUpdateRequest.getEmail()).ifPresent(userForUpdate::setEmail);
        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            Optional.ofNullable(userUpdateRequest.getRole()).ifPresent(userForUpdate::setRole);
            Optional.ofNullable(userUpdateRequest.getStatus()).ifPresent(userForUpdate::setStatus);
        }

        return userRepository.save(userForUpdate);
    }

    @Override
    public void delete(String userId) {
        throwIfUserNotFoundById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public boolean validateEntityOwnerOrAdmin(User authUser, String userId) {
        return Objects.equals(authUser.getId(), userId) || authUser.getRole() == UserRole.ADMIN;
    }

    public void throwIfUserExistsByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            log.error("[USER_SERVICE_FACADE]: User already exists by email: {}", email);
            throw new EntityExistsException("User already exists!");
        });

    }

    public User throwIfUserNotFoundById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[USER_SERVICE_FACADE]: User not found by id: {}", userId);
                    return new EntityNotFoundException("User not found!");
                });
    }

    public void throwIfUserWithSameEmailExists(String email) {
        User authenticatedUser = profileService.getAuthenticatedUser();

        if (authenticatedUser.getEmail().equals(email)) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            throw new EntityExistsException("User with this email already exists");
        }
    }
}
