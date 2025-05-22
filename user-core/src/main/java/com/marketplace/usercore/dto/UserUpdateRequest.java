package com.marketplace.usercore.dto;

import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {

    private String email;

    private UserStatus status;

    private UserRole role;

}
