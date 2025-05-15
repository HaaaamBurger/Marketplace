package com.marketplace.main.web.view;

import com.marketplace.usercore.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class MainModelAttributes {

    @ModelAttribute
    public void addRequestToModel(Model model, HttpServletRequest request) {
        model.addAttribute("request", request.getRequestURI());
    }

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            model.addAttribute("authUser", user);
        }

    }

}
