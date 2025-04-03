package com.marketplace.auth.web.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @PostMapping("/sign-in")
    public void signIn() {
        System.out.println("Hello world");
    }
}
