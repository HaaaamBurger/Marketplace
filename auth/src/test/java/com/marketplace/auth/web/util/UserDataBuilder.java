package com.marketplace.auth.web.util;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;

public class UserDataBuilder {
    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .email("test@gmail.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .password("testPassword1");
    }
}
