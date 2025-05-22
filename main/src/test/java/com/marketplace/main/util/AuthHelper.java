package com.marketplace.main.util;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.main.util.builder.AuthRequestDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Component
public class AuthHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Cookie signIn(User user, MockMvc mockMvc) throws Exception {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/sign-in")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"))
                .andExpect(redirectedUrl("/home"))
                .andReturn();

        Cookie accessCookieAccess = mvcResult.getResponse().getCookie(COOKIE_ACCESS_TOKEN);
        assertThat(accessCookieAccess).isNotNull();

        return accessCookieAccess;
    }

    public Map<String, Object> requireModel(MvcResult mvcResult) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        Map<String, Object> model = modelAndView.getModel();
        assertThat(model).isNotNull();

        return model;
    }

}
