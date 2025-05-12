package com.marketplace.main.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.exception.ExceptionResponse;
import com.marketplace.auth.exception.ExceptionType;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.auth.web.model.UserStatus;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.main.util.UserRequestDataBuilder;
import com.marketplace.main.util.UserDataBuilder;
import com.marketplace.user.web.dto.UserRequest;
import com.marketplace.user.web.dto.UserStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.marketplace.auth.security.service.JwtService.AUTHORIZATION_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

//    @Test
//    public void findById_ThenReturnUser() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        userRepository.save(user);
//
//        String response = mockMvc.perform(get("/users/{userId}", user.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        User responseUser = objectMapper.readValue(response, User.class);
//
//        assertThat(responseUser).isNotNull();
//        assertThat(responseUser.getId()).isEqualTo(user.getId());
//    }
//
//    @Test
//    public void findById_ThenThrowException_WhenUserNotFound() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        String userId = String.valueOf(UUID.randomUUID());
//
//        String response = mockMvc.perform(get("/users/{userId}", userId)
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isNotFound())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("User not found!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/users/%s", userId);
//    }
//
//    @Test
//    public void createUser_shouldReturnCreatedUser() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        UserRequest userRequest = UserRequestDataBuilder.withAllFields().build();
//
//        String response = mockMvc.perform(post("/users")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        User responseUser = objectMapper.readValue(response, User.class);
//
//        assertThat(responseUser).isNotNull();
//        assertThat(responseUser.getEmail()).isEqualTo(userRequest.getEmail());
//    }
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
//    @Test
//    public void findAll_ThenReturnAllUsers() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        User user1 = UserDataBuilder.buildUserWithAllFields()
//                .email("test1@gmail.com")
//                .build();
//        User user2 = UserDataBuilder.buildUserWithAllFields()
//                .email("test2@gmail.com")
//                .build();
//
//        userRepository.saveAll(List.of(user1, user2));
//
//        String response = mockMvc.perform(get("/users")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        List<User> usersResponse = objectMapper.readValue(response, new TypeReference<>() {});
//
//        assertThat(usersResponse).isNotNull();
//        assertThat(usersResponse.size()).isEqualTo(3);
//        assertThat(usersResponse.get(0).getRole()).isEqualTo(UserRole.ADMIN);
//        assertThat(usersResponse.get(1).getId()).isEqualTo(user1.getId());
//        assertThat(usersResponse.get(1).getEmail()).isEqualTo(user1.getEmail());
//        assertThat(usersResponse.get(2).getId()).isEqualTo(user2.getId());
//        assertThat(usersResponse.get(2).getEmail()).isEqualTo(user2.getEmail());
//    }
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
