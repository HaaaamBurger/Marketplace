package com.marketplace.auth.util;

import com.marketplace.auth.web.rest.dto.AuthRequest;

public class AuthRequestDataBuilder {
    public static AuthRequest.AuthRequestBuilder withAllFields() {
        return AuthRequest.builder()
                .email("test@gmail.com")
                .password("testPassword1");
    }
}
