package com.marketplace.usercore.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
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
        ifUserExistsByEmailThrow(userRequest.getEmail());
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
        return findUserByIdOrThrow(userId);
    }

    @Override
    public User update(String userId, UserRequest userRequest) {
        User user = findUserByIdOrThrow(userId);

        Optional.ofNullable(userRequest.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userRequest.getRole()).ifPresent(user::setRole);
        Optional.ofNullable(userRequest.getPassword()).ifPresent(requestPassword -> user.setPassword(passwordEncoder.encode(requestPassword)));

        return userRepository.save(user);
    }

    @Override
    public void updateStatus(String userId, UserStatus status) {
        User user = findUserByIdOrThrow(userId);
        user.setStatus(status);

        userRepository.save(user);
    }

    @Override
    public void delete(String userId) {
        findUserByIdOrThrow(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public boolean validateEntityOwnerOrAdmin(User user, String ownerId) {
        return Objects.equals(user.getId(), ownerId) || user.getRole() == UserRole.ADMIN;
    }

    public void ifUserExistsByEmailThrow(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            log.error("[MONGO_USER_SERVICE]: User already exists by email: {}", email);
            throw new EntityExistsException("User already exists!");
        }
    }

    public User findUserByIdOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[MONGO_USER_SERVICE]: User not found by id: {}", userId);
                    return new EntityNotFoundException("User not found!");
                });
    }
}
