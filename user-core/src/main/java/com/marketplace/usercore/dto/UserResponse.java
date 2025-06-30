package com.marketplace.usercore.dto;

import com.marketplace.common.dto.BaseResponse;
import lombok.Data;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.model.UserStatus;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends BaseResponse {

    private String email;

    private UserRole role;

    private UserStatus status;

}
