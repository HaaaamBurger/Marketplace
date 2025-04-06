package com.marketplace.auth.web.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;

import java.util.UUID;

public class UserDataBuilder {
    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1");
    }
}
