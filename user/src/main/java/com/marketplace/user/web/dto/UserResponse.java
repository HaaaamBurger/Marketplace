package com.marketplace.user.web.dto;

import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.model.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private String id;

    private String email;

    private UserRole role;

    private UserStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
