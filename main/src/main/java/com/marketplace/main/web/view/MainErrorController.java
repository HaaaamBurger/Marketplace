package com.marketplace.main.web.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Controller
public class MainErrorController implements ErrorController {

    @RequestMapping("/error")
    public String getError(HttpServletRequest request, Model model) {

        Object message = request.getSession().getAttribute("message");
        Object status = request.getSession().getAttribute("status");

        model.addAttribute("message", message == null ? "Something went wrong!" : message);
        model.addAttribute("status", status == null ? SC_BAD_REQUEST : status);
        model.addAttribute("path", request.getRequestURI());

        return "error";
    }

}
