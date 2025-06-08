package com.marketplace.usercore.service;

import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.security.AuthenticationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserSettingsService userSettingsService;

    private final AuthenticationUserService authenticationUserService;

    private final UserRepository userRepository;

    public User update(String userId, ProfileUpdateRequest profileUpdateRequest) {

        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        if (!Objects.equals(authenticatedUser.getId(), userId)) {
            log.error("[PROFILE_SERVICE]: User {} is not owner", authenticatedUser.getId());
            throw new AccessDeniedException("Access denied!");
        }

        User userForUpdate = userSettingsService.throwIfUserNotFoundById(userId);
        if (!profileUpdateRequest.getEmail().equals(userForUpdate.getEmail())) {
            userSettingsService.throwIfUserWithSameEmailExists(profileUpdateRequest.getEmail());
        }

        Optional.ofNullable(profileUpdateRequest.getEmail()).ifPresent(userForUpdate::setEmail);
        return userRepository.save(userForUpdate);
    }

}
