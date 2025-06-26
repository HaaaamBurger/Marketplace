package org.marketplace.util.builders;

import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;

public class UserUpdateRequestDataBuilder {
    public static UserUpdateRequest.UserUpdateRequestBuilder buildUserWithAllFields() {
        return UserUpdateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE);
    }
}
