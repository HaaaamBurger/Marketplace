package com.marketplace.auth.web.rest;

import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody @Valid AuthRequest authRequest, HttpServletResponse httpServletResponse) {
        AuthResponse authResponse = authenticationService.signIn(authRequest, httpServletResponse);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody @Valid AuthRequest authRequest) {
        authenticationService.signUp(authRequest);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid AuthRefreshRequest authRefreshRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(authRefreshRequest));
    }

}
