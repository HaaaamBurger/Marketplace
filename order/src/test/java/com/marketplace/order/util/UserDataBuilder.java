package com.marketplace.order.util;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;

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
