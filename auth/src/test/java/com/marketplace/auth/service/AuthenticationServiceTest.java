package com.marketplace.auth.service;

import com.marketplace.auth.exception.CredentialException;
import com.marketplace.auth.exception.EntityExistsException;
import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import com.marketplace.auth.web.util.AuthRequestDataBuilder;
import com.marketplace.common.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthenticationServiceTest {

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    public void signIn_shouldReturnPairOfTokens() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        User mockUser = mock(User.class);
        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";
        String mockEncodedPassword = "mockEncodedPassword";

        when(mockUser.getPassword()).thenReturn(mockEncodedPassword);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(authRequest.getPassword(), mockEncodedPassword)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationService.signIn(authRequest);

        assertNotNull(authResponse);
        assertThat(mockAccessToken).isEqualTo(authResponse.getAccessToken());
        assertThat(mockRefreshToken).isEqualTo(authResponse.getRefreshToken());
    }

    @Test
    public void signIn_shouldThrowException_WhenUserNotFound() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.findByEmail((authRequest.getEmail()))).thenReturn(Optional.empty());

        String exceptionMessage = assertThrows(CredentialException.class, () -> authenticationService.signIn(authRequest)).getMessage();
        assertThat(exceptionMessage).isNotBlank();
        assertThat(exceptionMessage).isEqualTo("Wrong credentials!");
    }

    @Test
    public void signIn_shouldThrowException_WhenPasswordNotMatched() {
        User mockUser = mock(User.class);
        String mockEncodedPassword = "mockEncodedPassword";
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.findByEmail((authRequest.getEmail()))).thenReturn(Optional.of(mockUser));
        when(mockUser.getPassword()).thenReturn(mockEncodedPassword);
        when(passwordEncoder.matches(authRequest.getPassword(), mockEncodedPassword)).thenReturn(false);

        String exceptionMessage = assertThrows(CredentialException.class, () -> authenticationService.signIn(authRequest)).getMessage();
        assertThat(exceptionMessage).isNotBlank();
        assertThat(exceptionMessage).isEqualTo("Wrong credentials!");
    }

    @Test
    public void signUp_shouldReturnMessage() {
        String encodedPassword = "encodedPassword";
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn(encodedPassword);
        authenticationService.signUp(authRequest);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(authRequest.getEmail());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    public void signUp_shouldThrowException_WhenUserAlreadyExists() {
        User mockUser = mock(User.class);
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> authenticationService.signUp(authRequest))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User already exists!");
    }

    @Test
    public void refreshToken_shouldReturnNewPairOfTokens() {
        String mockValidRefreshToken = "validRefreshToken";
        String mockSubject = "mockSubject";
        String mockAccessToken = "newAccessToken";
        String mockRefreshToken = "newRefreshToken";
        User mockUser = mock(User.class);

        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder()
                .refreshToken(mockValidRefreshToken)
                .build();

        when(jwtService.extractSubject(mockValidRefreshToken)).thenReturn(mockSubject);
        when(userDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUser);
        when(jwtService.isTokenValid(mockValidRefreshToken, mockUser)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationService.refreshToken(authRefreshRequest);

        assertNotNull(authResponse);
        assertThat(authResponse.getAccessToken()).isEqualTo(mockAccessToken);
        assertThat(authResponse.getRefreshToken()).isEqualTo(mockRefreshToken);
    }

    @Test
    public void refreshToken_shouldThrowException_WhenTokenNotValid() {
        String mockValidRefreshToken = "validRefreshToken";
        String mockSubject = "mockSubject";
        User mockUser = mock(User.class);

        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder()
                .refreshToken(mockValidRefreshToken)
                .build();

        when(jwtService.extractSubject(mockValidRefreshToken)).thenReturn(mockSubject);
        when(userDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUser);
        when(jwtService.isTokenValid(mockValidRefreshToken, mockUser)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.refreshToken(authRefreshRequest))
                .isInstanceOf(TokenNotValidException.class)
                .hasMessage("Token not valid!");
    }
}
