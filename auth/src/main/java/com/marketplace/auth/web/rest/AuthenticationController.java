package com.marketplace.auth.web.rest;

import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.signIn(authRequest));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.signUp(authRequest));
    }
}
