package com.marketplace.usercore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import com.marketplace.usercore.model.UserRole;

@Data
@Builder
public class UserRequest {

    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid e-mail address")
    private String email;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotBlank(message = "Password cannot be blank")
    private String password;

}
