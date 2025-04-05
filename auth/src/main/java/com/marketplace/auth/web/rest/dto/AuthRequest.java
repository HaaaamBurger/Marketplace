package com.marketplace.auth.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRequest {

    @NotNull(message = "Email cannot be empty")
    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid e-mail address")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Must be a valid password")
    private String password;

}
