package com.marketplace.main.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.common.exception.ExceptionResponse;
import com.marketplace.common.exception.ExceptionType;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.UserDataBuilder;
import com.marketplace.user.web.dto.UserCreateRequest;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void createUser_shouldReturnCreatedUser() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1")
                .build();

        userRepository.save(user);

        String response = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, authHelper.createAuth(user))
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
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.USER)
                .build();
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1")
                .build();

        userRepository.save(user);

        String contentAsString = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, authHelper.createAuth(user))
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
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        User createdUser = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.USER)
                .password("testPassword1")
                .build();

        userRepository.saveAll(List.of(user, createdUser));

        String contentAsString = mockMvc.perform(post("/users")
                        .header(AUTHORIZATION_HEADER, authHelper.createAuth(user))
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
}
