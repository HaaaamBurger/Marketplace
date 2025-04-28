package com.marketplace.main.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.common.exception.ExceptionResponse;
import com.marketplace.common.exception.ExceptionType;
import com.marketplace.common.model.UserStatus;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.main.util.builders.UserCreateRequestDataBuilder;
import com.marketplace.main.util.builders.UserDataBuilder;
import com.marketplace.user.web.dto.UserCreateRequest;
import com.marketplace.user.web.dto.UserStatusRequest;
import com.marketplace.user.web.dto.UserUpdateRequest;
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

import static com.marketplace.auth.security.JwtService.AUTHORIZATION_HEADER;
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

    @Test
    public void createUser_shouldReturnCreatedUser() throws Exception {
        UserCreateRequest userCreateRequest = UserCreateRequestDataBuilder.withAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        String response = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        User responseUser = objectMapper.readValue(response, User.class);

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmail()).isEqualTo(userCreateRequest.getEmail());
    }

    @Test
    public void createUser_WhenRoleUser_ThenForbidden() throws Exception {
        UserCreateRequest userCreateRequest = UserCreateRequestDataBuilder.withAllFields().build();

        String userAuth = authHelper.createUserAuth();
        String contentAsString = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);

        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
    }

    @Test
    public void createUser_WhenAlreadyExists_ThenReturnBadRequest() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        UserCreateRequest userCreateRequest = UserCreateRequestDataBuilder.withAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        String contentAsString = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);

        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
        assertThat(exceptionResponse.getMessage()).isEqualTo("User already exists!");
    }

    @Test
    public void findAll_ThenReturnAllUsers() throws Exception {
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        User user2 = UserDataBuilder.buildUserWithAllFields()
                .email("test2@gmail.com")
                .build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.saveAll(List.of(user1, user2));

        String contentAsString = mockMvc.perform(get("/users")
                        .header(AUTHORIZATION_HEADER, adminAuth))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<User> usersResponse = objectMapper.readValue(contentAsString, new TypeReference<>() {});

        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.size()).isEqualTo(3);
        assertThat(usersResponse.get(0).getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(usersResponse.get(1).getId()).isEqualTo(user1.getId());
        assertThat(usersResponse.get(1).getEmail()).isEqualTo(user1.getEmail());
        assertThat(usersResponse.get(2).getId()).isEqualTo(user2.getId());
        assertThat(usersResponse.get(2).getEmail()).isEqualTo(user2.getEmail());
    }

    @Test
    public void findById_ThenReturnUser() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        String response = mockMvc.perform(get("/users/{userId}", user.getId())
                        .header(AUTHORIZATION_HEADER, adminAuth))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        User responseUser = objectMapper.readValue(response, User.class);

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getId()).isEqualTo(user.getId());
    }

    @Test
    public void findById_ThenThrowException_WhenUserNotFound() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());

        String adminAuth = authHelper.createAdminAuth();

        String response = mockMvc.perform(get("/users/{userId}", userId)
                        .header(AUTHORIZATION_HEADER, adminAuth))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThatUserNotFound(exceptionResponse, userId);
    }

    @Test
    public void update_ThenUpdate_AndReturnUser() throws Exception {
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.ADMIN)
                .build();
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        String response = mockMvc.perform(put("/users/{userId}", user.getId())
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        User responseUser = objectMapper.readValue(response, User.class);

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getId()).isEqualTo(user.getId());
        assertThat(responseUser.getEmail()).isEqualTo(userUpdateRequest.getEmail());
        assertThat(responseUser.getRole()).isEqualTo(userUpdateRequest.getRole());
    }

    @Test
    public void update_ThenUpdatePassword() throws Exception {
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .password("testPassword2")
                .build();
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        mockMvc.perform(put("/users/{userId}", user.getId())
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk());

        Optional<User> optionalUser = userRepository.findById(user.getId());
        assertThat(optionalUser).isPresent();

        boolean matches = passwordEncoder.matches(userUpdateRequest.getPassword(), optionalUser.get().getPassword());
        assertThat(matches).isTrue();
    }

    @Test
    public void update_ThenThrowException_WhenUserNotFound() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .email("test1@gmail.com")
                .build();
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String adminAuth = authHelper.createAdminAuth();

        String response = mockMvc.perform(put("/users/{userId}", userId)
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThatUserNotFound(exceptionResponse, userId);
    }

    @Test
    public void updateStatus_ThenStatusUpdates() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(userId)
                .build();
        UserStatusRequest userStatusRequest = UserStatusRequest.builder()
                .userId(userId)
                .status(UserStatus.BLOCKED)
                .build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        mockMvc.perform(put("/users/status")
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userStatusRequest)))
                .andExpect(status().isOk());

        Optional<User> optionalUser = userRepository.findById(userId);

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getId()).isEqualTo(userStatusRequest.getUserId());
        assertThat(optionalUser.get().getStatus()).isEqualTo(userStatusRequest.getStatus());
    }

    @Test
    public void updateStatus_ThenThrowException_WhenUserNotFound() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());
        UserStatusRequest userStatusRequest = UserStatusRequest.builder()
                .userId(userId)
                .status(UserStatus.BLOCKED)
                .build();

        String adminAuth = authHelper.createAdminAuth();

        String response = mockMvc.perform(put("/users/status")
                        .header(AUTHORIZATION_HEADER, adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userStatusRequest)))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThatUserNotFound(exceptionResponse, userId);
    }

    @Test
    public void delete_ThenDeleteUser() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String adminAuth = authHelper.createAdminAuth();
        userRepository.save(user);

        mockMvc.perform(delete("/users/{userId}", user.getId())
                        .header(AUTHORIZATION_HEADER, adminAuth))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Optional<User> optionalUser = userRepository.findById(user.getId());

        assertThat(optionalUser).isNotPresent();
    }

    @Test
    public void delete_ThenThrowException_WhenUserNotFound() throws Exception {
        String userId = String.valueOf(UUID.randomUUID());

        String adminAuth = authHelper.createAdminAuth();

        String response = mockMvc.perform(delete("/users/{userId}", userId)
                        .header(AUTHORIZATION_HEADER, adminAuth))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThatUserNotFound(exceptionResponse, userId);
    }

    private void assertThatUserNotFound(ExceptionResponse exceptionResponse, String userId) {
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
        assertThat(exceptionResponse.getMessage()).isEqualTo("User not found by id: " + userId);
    }
}
