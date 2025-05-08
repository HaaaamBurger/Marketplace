package com.marketplace.main.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.marketplace.common.model.ModelAttributes.REQUEST;

@Controller
@RequestMapping("/home")
public class MainController {

    @GetMapping
    public String getHome(HttpServletRequest httpServletRequest, Model model) {
        httpServletRequest.getRequestURI().startsWith("/api/home");
        model.addAttribute(REQUEST.getAttributeName(), httpServletRequest);
        return "home";
    }
}
