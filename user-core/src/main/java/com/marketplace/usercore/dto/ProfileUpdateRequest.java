package com.marketplace.usercore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileUpdateRequest {

    private String email;

}
