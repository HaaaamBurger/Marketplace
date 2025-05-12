package com.marketplace.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

}
