package com.marketplace.usercore.dto;

import com.marketplace.usercore.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatusRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Status is required")
    private UserStatus status;

}
