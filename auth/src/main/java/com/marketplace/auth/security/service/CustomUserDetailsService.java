package com.marketplace.auth.security.service;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> (UserDetails) user)
                .orElseThrow(() -> {
                    log.error("[CUSTOM_USER_DETAILS_SERVICE]: User not found by username: {}", username);
                    return new UsernameNotFoundException("User does not exist!");
                });
    }

    public void validateUserNotBlockedOrThrow(UserDetails userDetails) {
        User user = (User) userDetails;

        if (user.getStatus() == UserStatus.BLOCKED) {
            log.error("[CUSTOM_USER_DETAILS_SERVICE]: User {} cannot access this resource. Status is {}", user.getId(), UserStatus.BLOCKED);
            throw new AccessDeniedException("Forbidden, not enough access!");
        }
    }

}
