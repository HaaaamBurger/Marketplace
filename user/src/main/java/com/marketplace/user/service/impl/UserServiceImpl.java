package com.marketplace.user.service.impl;

import com.marketplace.auth.exception.EntityExistsException;
import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.common.model.UserStatus;
import com.marketplace.user.service.UserService;
import com.marketplace.user.web.dto.UserRequest;
import com.marketplace.user.web.util.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserEntityMapper userEntityMapper;

    @Override
    public User create(UserRequest userRequest) {
        throwExceptionIfUserExistsByEmail(userRequest.getEmail());
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
        return findUserByIdOrThrowException(userId);
    }

    @Override
    public User update(String userId, UserRequest userRequest) {
        User user = findUserByIdOrThrowException(userId);

        Optional.ofNullable(userRequest.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userRequest.getRole()).ifPresent(user::setRole);
        Optional.ofNullable(userRequest.getPassword()).ifPresent(requestPassword -> user.setPassword(passwordEncoder.encode(requestPassword)));

        return userRepository.save(user);
    }

    @Override
    public void updateStatus(String userId, UserStatus status) {
        User user = findUserByIdOrThrowException(userId);
        user.setStatus(status);

        userRepository.save(user);
    }

    @Override
    public void delete(String userId) {
        findUserByIdOrThrowException(userId);
        userRepository.deleteById(userId);
    }

    private void throwExceptionIfUserExistsByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            log.error("[USER_SERVICE_IMPL]: User already exists by email: {}", email);
            throw new EntityExistsException("User already exists!");
        }
    }

    private User findUserByIdOrThrowException(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[USER_SERVICE_IMPL]: User not found by id: {}", userId);
                    return new EntityNotFoundException("User not found!");
                });
    }
}
