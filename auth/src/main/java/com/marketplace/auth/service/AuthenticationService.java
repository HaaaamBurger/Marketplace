package com.marketplace.auth.service;

import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;

import javax.security.auth.login.CredentialException;

public interface AuthenticationService {
    AuthResponse signIn(AuthRequest authRequest);

    String signUp(AuthRequest authRequest);

    AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest);
}
