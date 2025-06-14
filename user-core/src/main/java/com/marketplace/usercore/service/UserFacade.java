package com.marketplace.usercore.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.mapper.SimpleUserMapper;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.security.AuthenticationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class UserFacade implements UserCrudService, UserSettingsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationUserService authenticationUserService;

    private final SimpleUserMapper simpleUserMapper;

    @Override
    public User create(UserRequest userRequest) {
        throwIfUserExistsByEmail(userRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        User user = simpleUserMapper.mapUserRequestDtoToUser(userRequest).toBuilder()
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

        if (!userUpdateRequest.getEmail().equals(userForUpdate.getEmail())) {
            throwIfUserWithSameEmailExists(userUpdateRequest.getEmail());
            Optional.ofNullable(userUpdateRequest.getEmail()).ifPresent(userForUpdate::setEmail);
        }

        Optional.ofNullable(userUpdateRequest.getRole()).ifPresent(userForUpdate::setRole);
        Optional.ofNullable(userUpdateRequest.getStatus()).ifPresent(userForUpdate::setStatus);

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
