package com.marketplace.main.util;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.main.util.builder.AuthRequestDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Component
public class AuthHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public JwtCookiePayload signIn(User user, MockMvc mockMvc) throws Exception {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/sign-in")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/products/all"))
                .andExpect(redirectedUrl("/products/all"))
                .andReturn();

        Cookie accessCookie = mvcResult.getResponse().getCookie(COOKIE_ACCESS_TOKEN);
        Cookie refreshCookie = mvcResult.getResponse().getCookie(COOKIE_REFRESH_TOKEN);

        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        return JwtCookiePayload.builder()
                .accessCookie(accessCookie)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Data
    @Builder
    public static class JwtCookiePayload {

        private Cookie accessCookie;

        private Cookie refreshCookie;

    }


    public Map<String, Object> requireModel(MvcResult mvcResult) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        Map<String, Object> model = modelAndView.getModel();
        assertThat(model).isNotNull();

        return model;
    }

    public String requireViewName(MvcResult mvcResult) {
        ModelAndView modelAndView = mvcResult.getModelAndView();

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isNotNull();

        return modelAndView.getViewName();
    }

}
