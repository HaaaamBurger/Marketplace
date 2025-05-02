package com.marketplace.main.util;

import com.marketplace.auth.web.model.UserRole;
import com.marketplace.user.web.dto.UserRequest;

public class UserRequestDataBuilder {

    public static UserRequest.UserRequestBuilder withAllFields() {
        return UserRequest.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1");
    }

}
