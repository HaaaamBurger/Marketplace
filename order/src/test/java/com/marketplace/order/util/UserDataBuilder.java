package com.marketplace.order.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.model.UserStatus;

import java.util.UUID;

public class UserDataBuilder {

    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .email(String.valueOf(UUID.randomUUID()))
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .password(String.valueOf(UUID.randomUUID()));
    }

}
