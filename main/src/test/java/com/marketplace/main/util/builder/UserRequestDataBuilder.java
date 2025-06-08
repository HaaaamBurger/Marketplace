package com.marketplace.main.util.builder;

import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.model.UserRole;

public class UserRequestDataBuilder {

    public static UserRequest.UserRequestBuilder withAllFields() {
        return UserRequest.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1");
    }

}
