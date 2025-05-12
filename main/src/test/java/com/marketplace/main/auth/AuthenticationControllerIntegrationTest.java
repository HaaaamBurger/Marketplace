package com.marketplace.main.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.exception.ExceptionResponse;
import com.marketplace.auth.exception.ExceptionType;
import com.marketplace.main.MainApplication;
import com.marketplace.main.exception.MainExceptionHandler;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.service.JwtService;
import com.marketplace.main.util.AuthRequestDataBuilder;
import com.marketplace.main.util.UserDataBuilder;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MainExceptionHandler.class)
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach()
    public void setUp() {
        userRepository.deleteAll();
    }

//    @Test
//    public void signUp_shouldCreateUser() throws Exception {
//        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
//
//         mockMvc.perform(post("/auth/sign-up")
//                        .content(objectMapper.writeValueAsString(authRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//
//        Optional<User> optionalUser = userRepository.findByEmail(authRequest.getEmail());
//
//        assertThat(optionalUser).isPresent();
//        User user = optionalUser.get();
//        assertThat(user.getEmail()).isEqualTo(authRequest.getEmail());
//
//        boolean matches = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
//
//        assertThat(matches).isTrue();
//        assertThat(user.getEmail()).isEqualTo(authRequest.getEmail());
//        assertThat(user.getCreatedAt()).isNotNull();
//        assertThat(user.getUpdatedAt()).isNotNull();
//        assertThat(user.getCreatedAt()).isEqualToIgnoringNanos(user.getUpdatedAt());
//    }
//
//    @Test
//    public void signUp_shouldThrowException_WhenSignUserAlreadyExists() throws Exception {
//        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
//        userRepository.save(UserDataBuilder.buildUserWithAllFields().build());
//
//        String contentAsString = mockMvc.perform(post("/auth/sign-up")
//                        .content(objectMapper.writeValueAsString(authRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("User already exists!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/auth/sign-up");
//    }
//
//    @Test
//    public void signIn_shouldReturnPairOfTokens() throws Exception {
//        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
//        User savedUser = userRepository.save(UserDataBuilder.buildUserWithAllFields()
//                        .password(passwordEncoder.encode(authRequest.getPassword()))
//                .build());
//
//        String contentAsString = mockMvc.perform(post("/auth/sign-in")
//                        .content(objectMapper.writeValueAsString(authRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        AuthResponse authResponse = objectMapper.readValue(contentAsString, AuthResponse.class);
//
//        assertThat(authResponse).isNotNull();
//        assertThat(authResponse.getAccessToken()).isNotBlank();
//        assertThat(authResponse.getRefreshToken()).isNotBlank();
//        assertThat(jwtService.isTokenValid(authResponse.getAccessToken(), savedUser)).isTrue();
//        assertThat(jwtService.isTokenValid(authResponse.getRefreshToken(), savedUser)).isTrue();
//    }
//
//    @Test
//    public void signIn_shouldThrowException_WhenUserDoesNotExist() throws Exception {
//        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
//
//        String contentAsString = mockMvc.perform(post("/auth/sign-in")
//                        .content(objectMapper.writeValueAsString(authRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Wrong credentials!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/auth/sign-in");
//    }
//
//    @Test
//    public void signIn_shouldThrowException_WhenUserPasswordDoesNotMatch() throws Exception {
//        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
//        userRepository.save(UserDataBuilder.buildUserWithAllFields()
//                .password(passwordEncoder.encode(authRequest.getPassword()))
//                .build());
//
//        authRequest.setPassword("testPassword2");
//
//        String contentAsString = mockMvc.perform(post("/auth/sign-in")
//                        .content(objectMapper.writeValueAsString(authRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Wrong credentials!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/auth/sign-in");
//    }
//
//    @Test
//    public void refreshToken_shouldReturnPairOfTokens() throws Exception {
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//        String refreshToken = jwtService.generateRefreshToken(user);
//        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder().refreshToken(refreshToken).build();
//
//        userRepository.save(user);
//
//        String contentAsString = mockMvc.perform(post("/auth/refresh-token")
//                        .content(objectMapper.writeValueAsString(authRefreshRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        AuthResponse authResponse = objectMapper.readValue(contentAsString, AuthResponse.class);
//
//        assertThat(authResponse).isNotNull();
//        assertThat(authResponse.getAccessToken()).isNotBlank();
//        assertThat(authResponse.getRefreshToken()).isNotBlank();
//        assertThat(jwtService.isTokenValid(authResponse.getAccessToken(), user)).isTrue();
//        assertThat(jwtService.isTokenValid(authResponse.getRefreshToken(), user)).isTrue();
//    }
//
//    @Test
//    public void refreshToken_shouldThrowException_WhenUserDoesNotExist() throws Exception {
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        String refreshToken = jwtService.generateRefreshToken(user);
//        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder().refreshToken(refreshToken).build();
//
//
//        String contentAsString = mockMvc.perform(post("/auth/refresh-token")
//                        .content(objectMapper.writeValueAsString(authRefreshRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Authentication required, please sign in");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/auth/refresh-token");
//    }
//
//    @Test
//    public void refreshToken_shouldThrowException_WhenRefreshTokenNotValid() throws Exception {
//        User user = UserDataBuilder.buildUserWithAllFields().build();
//
//        String refreshToken = jwtService.generateRefreshTokenWithExpiration(user, 0);
//        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder().refreshToken(refreshToken).build();
//
//        userRepository.save(user);
//
//        String contentAsString = mockMvc.perform(post("/auth/refresh-token")
//                        .content(objectMapper.writeValueAsString(authRefreshRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(contentAsString, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Token not valid!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/auth/refresh-token");
//    }
}