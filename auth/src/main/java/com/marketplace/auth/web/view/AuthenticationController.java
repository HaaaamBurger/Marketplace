package com.marketplace.auth.web.view;

import com.marketplace.auth.service.AuthenticationManagerService;
import com.marketplace.auth.web.validator.SignInValidator;
import com.marketplace.auth.web.validator.SignUpValidator;
import com.marketplace.auth.web.dto.AuthRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthenticationController {

    private final AuthenticationManagerService authenticationManagerService;

    private final SignUpValidator signUpValidator;

    private final SignInValidator signInValidator;

    @GetMapping("/sign-in")
    public String signIn(Model model) {
        model.addAttribute("authRequest", AuthRequest.builder().build());
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public String signIn(
            @Valid @ModelAttribute AuthRequest authRequest,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        signInValidator.validate(authRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            return "sign-in";
        }

        authenticationManagerService.signIn(authRequest, response);

        return "redirect:/products/all";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("authRequest", AuthRequest.builder().build());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid @ModelAttribute AuthRequest authRequest, BindingResult bindingResult) {

        signUpValidator.validate(authRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "sign-up";
        }

        authenticationManagerService.signUp(authRequest);
        return "redirect:/sign-in";

    }
}
