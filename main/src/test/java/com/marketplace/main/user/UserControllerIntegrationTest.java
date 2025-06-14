package com.marketplace.main.user;

import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.main.util.builder.UserRequestDataBuilder;
import com.marketplace.main.util.builder.UserUpdateRequestDataBuilder;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MainExceptionHandler.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthHelper authHelper;


    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void getProfile_ThenReturnUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        MvcResult mvcResult = mockMvc.perform(get("/profile")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        User modelUser = (User) model.get("authUser");

        assertThat(modelUser).isNotNull();
        assertThat(modelUser.getId()).isEqualTo(authUser.getId());
    }

    @Test
    public void createUser_WhenRoleAdmin_ShouldRedirectToUsersList() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        mockMvc.perform(post("/users/create")
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().is3xxRedirection());

        Optional<User> optionalUser = userRepository.findByEmail(userRequest.getEmail());

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getEmail()).isEqualTo(userRequest.getEmail());
    }

    @Test
    public void createUser_WhenRoleUser_ShouldRedirectToErrorPage() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(post("/users/create")
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().is3xxRedirection()).andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");
    }

    @Test
    public void createUser_WhenUserWithEmailExists_ShouldReturnValidationError() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(post("/users/create")
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().isOk()).andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        FieldError fieldError = ((BindingResult) model.get("org.springframework.validation.BindingResult.userRequest")).getFieldError();
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isEqualTo("This email already in use");
    }


    @Test
    public void createUser_WhenRoleUser_ThenRedirectToErrorPage() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(post("/users/create")
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");
    }

    @Test
    public void findAll_WhenRoleAdmin_ThenReturnAllUsers() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        User user2 = UserDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.saveAll(List.of(user1, user2));

        MvcResult mvcResult = mockMvc.perform(get("/users/all")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        List<UserResponse> userResponses = (List<UserResponse>) model.get("users");
        assertThat(userResponses).isNotNull();
        assertThat(userResponses.size()).isEqualTo(3);
        assertThat(userResponses).extracting(UserResponse::getEmail).containsExactlyInAnyOrder(authUser.getEmail(), user1.getEmail(), user2.getEmail());
    }

    @Test
    public void findAll_WhenRoleUser_ShouldRedirectToErrorPage() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        User user2 = UserDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.saveAll(List.of(user1, user2));

        MvcResult mvcResult = mockMvc.perform(get("/users/all")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");
    }

    @Test
    public void updateUser_WhenRoleAdmin_ThenRedirectToUsers() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        User user = UserDataBuilder.buildUserWithAllFields().email("test1@gmail.com").build();
        UserUpdateRequest userUpdateRequest = UserUpdateRequestDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(put("/users/{userId}/update", user.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userUpdateRequest.getEmail())
                        .param("status", String.valueOf(userUpdateRequest.getStatus()))
                        .param("role", String.valueOf(userUpdateRequest.getRole())))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/users/all");

        Optional<User> optionalUser = userRepository.findByEmail(userUpdateRequest.getEmail());
        assertThat(optionalUser).isPresent();
    }

    @Test
    public void updateUser_WhenRoleUser_ThenRedirectToErrorPage() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        UserUpdateRequest userUpdateRequest = UserUpdateRequestDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(put("/users/{userId}/update", authUser.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("email", userUpdateRequest.getEmail())
                        .param("status", String.valueOf(userUpdateRequest.getStatus()))
                        .param("role", String.valueOf(userUpdateRequest.getRole())))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");

        Optional<User> optionalUser = userRepository.findByEmail(authUser.getEmail());
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getEmail()).isNotEqualTo(userUpdateRequest.getEmail());
    }

    @Test
    public void delete_WhenRoleAdmin_ThenRedirectToUsers() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().email("test1@gmail.com").build();
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.save(user);

        String redirectedUrl = mockMvc.perform(delete("/users/{userId}/delete", user.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/users/all");

        Optional<User> optionalUser = userRepository.findById(user.getId());
        assertThat(optionalUser).isNotPresent();
    }

    @Test
    public void deleteUser_WhenRoleUser_ShouldRedirectToErrorPage() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().email("test1@gmail.com").build();
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        userRepository.save(user);

        String redirectedUrl = mockMvc.perform(delete("/users/{userId}/delete", user.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");

        Optional<User> optionalUser = userRepository.findById(user.getId());
        assertThat(optionalUser).isPresent();
    }

    @Test
    public void delete_WhenRoleAdmin_AndUserNotFound() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        ModelAndView modelAndView = mockMvc.perform(delete("/users/{userId}/delete", userId)
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isNotFound())
                .andReturn()
                .getModelAndView();

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("error");
    }
}
