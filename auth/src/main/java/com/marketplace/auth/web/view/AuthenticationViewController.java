package com.marketplace.auth.web.view;

import com.marketplace.auth.service.AuthenticationService;
import com.marketplace.auth.service.impl.AuthenticationValidator;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthenticationViewController {

    private final AuthenticationService authenticationService;

    private final AuthenticationValidator authenticationValidator;

    @GetMapping("/sign-in")
    public String signIn(Model model) {
        model.addAttribute("authRequest", AuthRequest.builder().build());
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public String signIn(@Valid @ModelAttribute AuthRequest authRequest, BindingResult bindingResult) {
        return "sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("authRequest", AuthRequest.builder().build());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid @ModelAttribute AuthRequest authRequest, BindingResult bindingResult) {

        authenticationValidator.validate(authRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "sign-up";
        }

        authenticationService.signUp(authRequest);
        return "redirect:/sign-in";

    }
}
