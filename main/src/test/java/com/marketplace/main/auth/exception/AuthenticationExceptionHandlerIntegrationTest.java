package com.marketplace.main.auth.exception;

import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {"JWT_ACCESS_EXPIRATION_TIME = 0", "JWT_REFRESH_EXPIRATION_TIME = 0"})
public class AuthenticationExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();;
    }

    @Test
    public void getProducts_ShouldRedirectToSignIn_WhenTokenExpired() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(user, mockMvc);

        String redirectedUrl = mockMvc.perform(get("/users/all")
                        .cookie(jwtCookiePayload.getAccessCookie(), jwtCookiePayload.getRefreshCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/sign-in");
    }

}
