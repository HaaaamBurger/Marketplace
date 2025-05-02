package com.marketplace.auth.service;

import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;

public interface AuthenticationService {

    AuthResponse signIn(AuthRequest authRequest);

    void signUp(AuthRequest authRequest);

    AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest);

}
