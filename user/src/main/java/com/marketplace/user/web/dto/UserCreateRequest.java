package com.marketplace.user.web.dto;

import com.marketplace.auth.web.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateRequest {

    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid e-mail address")
    private String email;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotBlank(message = "Password cannot be blank")
    private String password;

}
