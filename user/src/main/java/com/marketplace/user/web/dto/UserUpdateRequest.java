package com.marketplace.user.web.dto;

import com.marketplace.auth.web.model.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {

    private String email;

    private UserRole role;

    private String password;

}
