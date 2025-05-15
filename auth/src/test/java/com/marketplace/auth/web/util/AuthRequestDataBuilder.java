package com.marketplace.auth.web.util;

import com.marketplace.auth.web.dto.AuthRequest;

public class AuthRequestDataBuilder {
    public static AuthRequest.AuthRequestBuilder withAllFields() {
        return AuthRequest.builder()
                .email("test@gmail.com")
                .password("testPassword1");
    }
}
