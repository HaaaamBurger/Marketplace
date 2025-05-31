package com.marketplace.usercore.security;

import com.marketplace.usercore.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationUserService {

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("[PROFILE_SERVICE]: Authentication is null");
            throw new AuthenticationCredentialsNotFoundException("Authentication is unavailable!");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        } else {
            log.error("[PROFILE_SERVICE]: {} is not instance of UserDetails", principal);
            throw new AuthenticationServiceException("User is not authenticated");
        }
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null;
    }
}
