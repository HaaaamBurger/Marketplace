package com.marketplace.usercore.service;

import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.mapper.SimpleUserMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoUserCrudService implements UserCrudService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final SimpleUserMapper simpleUserMapper;

    private final UserManagerService userManagerService;

    @Override
    public User create(UserRequest userRequest) {
        userManagerService.throwIfUserExistsByEmail(userRequest.getEmail());

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
        return userManagerService.throwIfUserNotFoundByIdOrGet(userId);
    }

    @Override
    public User update(String userId, UserUpdateRequest userUpdateRequest) {
        User userForUpdate = userManagerService.throwIfUserNotFoundByIdOrGet(userId);

        if (!userUpdateRequest.getEmail().equals(userForUpdate.getEmail())) {
            userManagerService.throwIfUserExistsByEmail(userUpdateRequest.getEmail());
            Optional.ofNullable(userUpdateRequest.getEmail()).ifPresent(userForUpdate::setEmail);
        }

        Optional.ofNullable(userUpdateRequest.getRole()).ifPresent(userForUpdate::setRole);
        Optional.ofNullable(userUpdateRequest.getStatus()).ifPresent(userForUpdate::setStatus);

        return userRepository.save(userForUpdate);
    }

    @Override
    public void delete(String userId) {
        userManagerService.throwIfUserNotFoundByIdOrGet(userId);
        userRepository.deleteById(userId);
    }

}
