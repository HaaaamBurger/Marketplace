package com.marketplace.auth.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;

public class UserDataBuilder {
    public static User.UserBuilder buildUserWithAllFields() {
        return User.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1");
    }
}
