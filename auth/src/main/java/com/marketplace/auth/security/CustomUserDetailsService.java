package com.marketplace.auth.security;

import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        if (username == null) {
            log.error("[CUSTOM_USER_DETAILS_SERVICE]: Username cannot be null");
            throw new UsernameNotFoundException("User does not exist!");
        }

        return userRepository.findByEmail(username)
                .map(user -> (UserDetails) user)
                .orElseThrow(() -> {
                    log.error("[CUSTOM_USER_DETAILS_SERVICE]: User not found by username: {}", username);
                    return new UsernameNotFoundException("User does not exist!");
                });
    }
}
