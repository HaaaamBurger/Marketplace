package com.marketplace.usercore.service;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultUserValidationService implements UserValidationService {

    @Override
    public boolean validateEntityOwnerOrAdmin(User authUser, String userId) {
        return Objects.equals(authUser.getId(), userId) || authUser.getRole() == UserRole.ADMIN;
    }

}
