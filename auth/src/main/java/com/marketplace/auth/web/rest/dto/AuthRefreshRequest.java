package com.marketplace.auth.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRefreshRequest {

    @NotBlank
    private String refreshToken;
}
