package com.marketplace.main.util.builders;

import com.marketplace.auth.web.rest.dto.AuthRequest;

public class AuthRequestDataBuilder {

    public static AuthRequest.AuthRequestBuilder withAllFields() {
        return AuthRequest.builder()
                .email("test@gmail.com")
                .password("testPassword1");
    }

}
