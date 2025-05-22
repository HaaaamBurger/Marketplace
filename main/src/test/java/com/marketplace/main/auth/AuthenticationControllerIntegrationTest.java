package com.marketplace.main.auth;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.auth.security.service.JwtService;
import com.marketplace.main.util.builder.AuthRequestDataBuilder;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MainExceptionHandler.class)
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach()
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void sigInView_ShouldReturnSigIn() throws Exception {
        mockMvc.perform(post("/sign-in"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-in"));
    }

    @Test
    public void sigUpView_ShouldReturnSigUp() throws Exception {
        mockMvc.perform(post("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"));
    }

    @Test
    public void signIn_shouldReturnCookie_ThenRedirectToHome() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/sign-in")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"))
                .andExpect(redirectedUrl("/home"))
                .andReturn();

        Cookie accessCookieAccess = mvcResult.getResponse().getCookie(COOKIE_ACCESS_TOKEN);
        Cookie refreshCookieAccess = mvcResult.getResponse().getCookie(COOKIE_REFRESH_TOKEN);

        assertThat(accessCookieAccess).isNotNull();
        assertThat(accessCookieAccess.getValue()).isNotNull();
        assertThat(jwtService.isTokenValid(accessCookieAccess.getValue(), user)).isTrue();
        assertThat(refreshCookieAccess).isNotNull();
        assertThat(refreshCookieAccess.getValue()).isNotNull();
        assertThat(jwtService.isTokenValid(refreshCookieAccess.getValue(), user)).isTrue();
    }

    @Test
    public void signIn_ShouldReturnSignIn_WhenUserDoesNotExist() throws Exception {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        MvcResult mvcResult = mockMvc.perform(post("/sign-in")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(view().name("sign-in"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = requireModel(mvcResult);

        BindingResult bindingResult = (BindingResult) model.get("org.springframework.validation.BindingResult.authRequest");
        assertThat(bindingResult.getFieldError("email")).isNotNull();
    }

    @Test
    public void signIn_ShouldReturnSignIn_WhenWrongPassword() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields()
                .password("testPassword12")
                .build();

        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/sign-in")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-in"))
                .andExpect(model().hasErrors())
                .andReturn();

        Map<String, Object> model = requireModel(mvcResult);

        BindingResult bindingResult = (BindingResult) model.get("org.springframework.validation.BindingResult.authRequest");
        assertThat(bindingResult.getFieldError("password")).isNotNull();

        Cookie accessCookieAccess = mvcResult.getResponse().getCookie(COOKIE_ACCESS_TOKEN);
        Cookie refreshCookieAccess = mvcResult.getResponse().getCookie(COOKIE_REFRESH_TOKEN);

        assertThat(accessCookieAccess).isNull();
        assertThat(refreshCookieAccess).isNull();
    }

    @Test
    public void signUp_ShouldCreateUser_ThenRedirectToSignIn() throws Exception {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        mockMvc.perform(post("/sign-up")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(view().name("redirect:/sign-in"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        Optional<User> optionalUser = userRepository.findByEmail(authRequest.getEmail());
        assertThat(optionalUser).isPresent();
        User user = optionalUser.get();
        assertThat(user.getEmail()).isEqualTo(authRequest.getEmail());
        assertThat(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualToIgnoringNanos(user.getUpdatedAt());
    }

    @Test
    public void signUp_shouldReturnSignUp_WhenUserAlreadyExists() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/sign-up")
                        .param("email", authRequest.getEmail())
                        .param("password", authRequest.getPassword()))
                .andExpect(view().name("sign-up"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = requireModel(mvcResult);

        BindingResult bindingResult = (BindingResult) model.get("org.springframework.validation.BindingResult.authRequest");
        assertThat(bindingResult.getFieldError("email")).isNotNull();
    }

    private Map<String, Object> requireModel(MvcResult mvcResult) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        Map<String, Object> model = modelAndView.getModel();
        Assertions.assertThat(model).isNotNull();

        return model;
    }

}