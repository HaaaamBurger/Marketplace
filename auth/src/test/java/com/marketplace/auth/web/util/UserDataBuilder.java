package com.marketplace.auth.web.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.model.UserStatus;

public class UserDataBuilder {
    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .email("test@gmail.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .password("testPassword1");
    }
}
