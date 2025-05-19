package com.marketplace.main.util.builder;

import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;

public class UserUpdateRequestDataBuilder {

    public static UserUpdateRequest.UserUpdateRequestBuilder buildUserWithAllFields() {
        return UserUpdateRequest.builder()
                .email("test@gmail.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE);
    }

}
