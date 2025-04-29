package com.marketplace.main.util;

import com.marketplace.auth.web.model.UserRole;
import com.marketplace.user.web.dto.UserCreateRequest;

public class UserCreateRequestDataBuilder {

    public static UserCreateRequest.UserCreateRequestBuilder withAllFields() {
        return UserCreateRequest.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1");
    }

}
