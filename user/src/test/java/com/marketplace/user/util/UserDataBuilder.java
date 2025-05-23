package com.marketplace.user.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.model.UserStatus;

public class UserDataBuilder {
    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .password("testPassword1");
    }
}
