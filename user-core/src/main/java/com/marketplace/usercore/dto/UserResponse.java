package com.marketplace.usercore.dto;

import lombok.Builder;
import lombok.Data;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;

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
