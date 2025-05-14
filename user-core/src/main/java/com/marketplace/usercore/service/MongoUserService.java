package com.marketplace.usercore.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.mapper.UserEntityMapper;
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
public final class MongoUserService implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserEntityMapper userEntityMapper;

    @Override
    public User create(UserRequest userRequest) {
        throwIfUserExistsByEmail(userRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        User user = userEntityMapper.mapRequestDtoToEntity(userRequest).toBuilder()
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
        User user = throwIfUserNotFoundById(userId);

        Optional.ofNullable(userUpdateRequest.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userUpdateRequest.getStatus()).ifPresent(user::setStatus);
        Optional.ofNullable(userUpdateRequest.getRole()).ifPresent(user::setRole);

        return userRepository.save(user);
    }

    @Override
    public void updateStatus(String userId, UserStatus status) {
        User user = throwIfUserNotFoundById(userId);
        user.setStatus(status);

        userRepository.save(user);
    }

    @Override
    public void delete(String userId) {
        throwIfUserNotFoundById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public boolean validateEntityOwnerOrAdmin(User user, String ownerId) {
        return Objects.equals(user.getId(), ownerId) || user.getRole() == UserRole.ADMIN;
    }

    public void throwIfUserExistsByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            log.error("[MONGO_USER_SERVICE]: User already exists by email: {}", email);
            throw new EntityExistsException("User already exists!");
        }
    }

    public User throwIfUserNotFoundById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[MONGO_USER_SERVICE]: User not found by id: {}", userId);
                    return new EntityNotFoundException("User not found!");
                });
    }
}
