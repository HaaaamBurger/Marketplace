package com.marketplace.auth.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRefreshRequest {

    @NotBlank
    private String refreshToken;
}
