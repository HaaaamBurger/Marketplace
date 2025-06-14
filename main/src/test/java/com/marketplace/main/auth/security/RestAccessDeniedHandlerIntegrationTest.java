package com.marketplace.main.auth.security;

import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RestAccessDeniedHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void handle_ShouldRedirectToErrorPage_WhenAccessNotDenied() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(get("/users")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(mvcResult.getResponse().getRedirectedUrl()).isNotNull();
        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/error");

        Integer status = (Integer) mvcResult.getRequest().getSession().getAttribute("status");
        assertThat(status).isNotNull();
        assertThat(status).isEqualTo(SC_FORBIDDEN);
        String message = mvcResult.getRequest().getSession().getAttribute("message").toString();
        assertThat(message).isNotNull();
        assertThat(message).isEqualTo("Access Denied");
    }

}
