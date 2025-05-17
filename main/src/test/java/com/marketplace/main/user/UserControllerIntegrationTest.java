package com.marketplace.main.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.main.util.builder.UserRequestDataBuilder;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void getProfile_ThenReturnUser() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        Cookie cookie = authHelper.signIn(user, mockMvc);

        userRepository.save(user1);

        MvcResult mvcResult = mockMvc.perform(get("/users/profile")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        User modelUser = (User) model.get("authUser");

        assertThat(modelUser).isNotNull();
        assertThat(modelUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void createUser_shouldRedirectToUsers() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        Cookie cookie = authHelper.signIn(user, mockMvc);

        mockMvc.perform(post("/users")
                        .cookie(cookie)
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().is3xxRedirection());

        Optional<User> optionalUser = userRepository.findByEmail(userRequest.getEmail());

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getEmail()).isEqualTo(userRequest.getEmail());
    }

    @Test
    public void createUser_shouldRedirectToHome_WhenNotAdmin() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
                .email("test1@gmail.com")
                .build();

        Cookie cookie = authHelper.signIn(user, mockMvc);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .cookie(cookie)
                        .param("email", userRequest.getEmail())
                        .param("role", String.valueOf(userRequest.getRole()))
                        .param("password", String.valueOf(userRequest.getPassword())))
                .andExpect(status().is3xxRedirection()).andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

//
//    @Test
//    public void createUser_WhenRoleUser_ThenForbidden() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        UserRequest userRequest = UserRequestDataBuilder.withAllFields().build();
//
//        String response = mockMvc.perform(post("/users")
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userRequest)))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/users");
//    }
//
//    @Test
//    public void createUser_WhenAlreadyExists_ThenReturnBadRequest() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//        UserRequest userRequest = UserRequestDataBuilder.withAllFields().build();
//
//        userRepository.save(user);
//
//        String response = mockMvc.perform(post("/users")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userRequest)))
//                .andExpect(status().isBadRequest())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("User already exists!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/users");
//    }
//
    @Test
    public void findAll_ThenReturnAllUsers() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .password(passwordEncoder.encode("testPassword1"))
                .build();
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        User user2 = UserDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();
        Cookie cookie = authHelper.signIn(user, mockMvc);

        userRepository.saveAll(List.of(user1, user2));

        MvcResult mvcResult = mockMvc.perform(get("/users")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        List<UserResponse> userResponses = (List<UserResponse>) model.get("users");
        assertThat(userResponses.size()).isEqualTo(3);
        assertThat(userResponses).extracting(UserResponse::getEmail).containsExactlyInAnyOrder(user.getEmail(), user1.getEmail(), user2.getEmail());
    }
//
//    @Test
//    public void update_ThenUpdate_AndReturnUser() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
//                .role(UserRole.ADMIN)
//                .build();
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        userRepository.save(user);
//
//        String response = mockMvc.perform(put("/users/{userId}", user.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userRequest)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        User responseUser = objectMapper.readValue(response, User.class);
//
//        assertThat(responseUser).isNotNull();
//        assertThat(responseUser.getId()).isEqualTo(user.getId());
//        assertThat(responseUser.getEmail()).isEqualTo(userRequest.getEmail());
//        assertThat(responseUser.getRole()).isEqualTo(userRequest.getRole());
//    }
//
//    @Test
//    public void update_ThenUpdatePassword() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        UserRequest userRequest = UserRequestDataBuilder.withAllFields()
//                .password("testPassword2")
//                .build();
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        userRepository.save(user);
//
//        mockMvc.perform(put("/users/{userId}", user.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userRequest)))
//                .andExpect(status().isOk());
//
//        Optional<User> optionalUser = userRepository.findById(user.getId());
//        assertThat(optionalUser).isPresent();
//
//        boolean matches = passwordEncoder.matches(userRequest.getPassword(), optionalUser.get().getPassword());
//        assertThat(matches).isTrue();
//    }
//
//    @Test
//    public void updateStatus_ThenStatusUpdates() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        String userId = String.valueOf(UUID.randomUUID());
//        User user = UserDataBuilder.buildUserWithAllFields()
//                .id(userId)
//                .build();
//        UserStatusRequest userStatusRequest = UserStatusRequest.builder()
//                .userId(userId)
//                .status(UserStatus.BLOCKED)
//                .build();
//
//        userRepository.save(user);
//
//        mockMvc.perform(put("/users/status")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userStatusRequest)))
//                .andExpect(status().isOk());
//
//        Optional<User> optionalUser = userRepository.findById(userId);
//
//        assertThat(optionalUser).isPresent();
//        assertThat(optionalUser.get().getId()).isEqualTo(userStatusRequest.getUserId());
//        assertThat(optionalUser.get().getStatus()).isEqualTo(userStatusRequest.getStatus());
//    }
//
//    @Test
//    public void delete_ThenDeleteUser() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        userRepository.save(user);
//
//        mockMvc.perform(delete("/users/{userId}", user.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        Optional<User> optionalUser = userRepository.findById(user.getId());
//
//        assertThat(optionalUser).isNotPresent();
//    }
}
