package com.marketplace.auth.service;

import com.marketplace.auth.web.dto.AuthRefreshRequest;
import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.auth.web.dto.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    AuthResponse signIn(AuthRequest authRequest, HttpServletResponse response);

    void signUp(AuthRequest authRequest);

    AuthResponse refreshToken(AuthRefreshRequest authRefreshRequest);

}
