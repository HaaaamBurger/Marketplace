package com.marketplace.auth.service;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.exception.CredentialException;
import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.security.CustomUserDetailsService;
import com.marketplace.auth.security.token.JwtService;
import com.marketplace.auth.web.dto.AuthRefreshRequest;
import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.auth.web.dto.AuthResponse;
import com.marketplace.auth.web.util.builder.AuthRequestDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthApplicationConfig.class)
public class AuthenticationManagerServiceTest {

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private AuthenticationManagerService authenticationManagerService;

    @Test
    public void signIn_shouldReturnPairOfTokens() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        User mockUser = mock(User.class);
        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";
        String mockEncodedPassword = "mockEncodedPassword";

        when(mockUser.getPassword()).thenReturn(mockEncodedPassword);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(authRequest.getPassword(), mockEncodedPassword)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationManagerService.signIn(authRequest, httpServletResponse);

        assertNotNull(authResponse);
        assertThat(mockAccessToken).isEqualTo(authResponse.getAccessToken());
        assertThat(mockRefreshToken).isEqualTo(authResponse.getRefreshToken());

        verify(mockUser).getPassword();
        verify(userRepository).findByEmail(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), mockEncodedPassword);
        verify(jwtService).generateAccessToken(mockUser);
        verify(jwtService).generateRefreshToken(mockUser);
    }

    @Test
    public void signIn_shouldThrowException_WhenUserNotFound() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        when(userRepository.findByEmail((authRequest.getEmail()))).thenReturn(Optional.empty());

        String exceptionMessage = assertThrows(CredentialException.class, () -> authenticationManagerService.signIn(authRequest, httpServletResponse)).getMessage();

        assertThat(exceptionMessage).isNotBlank();
        assertThat(exceptionMessage).isEqualTo("Wrong credentials!");

        verify(userRepository).findByEmail(authRequest.getEmail());
    }

    @Test
    public void signIn_shouldThrowException_WhenPasswordNotMatched() {
        User mockUser = mock(User.class);
        String mockEncodedPassword = "mockEncodedPassword";
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.findByEmail((authRequest.getEmail()))).thenReturn(Optional.of(mockUser));
        when(mockUser.getPassword()).thenReturn(mockEncodedPassword);
        when(passwordEncoder.matches(authRequest.getPassword(), mockEncodedPassword)).thenReturn(false);

        String exceptionMessage = assertThrows(CredentialException.class, () -> authenticationManagerService.signIn(authRequest, httpServletResponse)).getMessage();
        assertThat(exceptionMessage).isNotBlank();
        assertThat(exceptionMessage).isEqualTo("Wrong credentials!");

        verify(userRepository).findByEmail(authRequest.getEmail());
        verify(mockUser).getPassword();
        verify(passwordEncoder).matches(authRequest.getPassword(), mockEncodedPassword);
    }

    @Test
    public void signUp_shouldReturnMessage() {
        String encodedPassword = "encodedPassword";
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn(encodedPassword);

        authenticationManagerService.signUp(authRequest);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(authRequest.getEmail());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).existsByEmail(authRequest.getEmail());
        verify(passwordEncoder).encode(authRequest.getPassword());
    }

    @Test
    public void signUp_shouldThrowException_WhenUserAlreadyExists() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authenticationManagerService.signUp(authRequest))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User already exists!");

        verify(userRepository).existsByEmail(authRequest.getEmail());
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
        when(customUserDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUser);
        when(jwtService.isTokenValid(mockValidRefreshToken, mockUser)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationManagerService.refreshToken(authRefreshRequest);

        assertNotNull(authResponse);
        assertThat(authResponse.getAccessToken()).isEqualTo(mockAccessToken);
        assertThat(authResponse.getRefreshToken()).isEqualTo(mockRefreshToken);

        verify(jwtService).extractSubject(mockValidRefreshToken);
        verify(customUserDetailsService).loadUserByUsername(mockSubject);
        verify(jwtService).isTokenValid(mockValidRefreshToken, mockUser);
        verify(jwtService).generateAccessToken(mockUser);
        verify(jwtService).generateRefreshToken(mockUser);
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
        when(customUserDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUser);
        when(jwtService.isTokenValid(mockValidRefreshToken, mockUser)).thenReturn(false);

        assertThatThrownBy(() -> authenticationManagerService.refreshToken(authRefreshRequest))
                .isInstanceOf(TokenNotValidException.class)
                .hasMessage("Token not valid!");

        verify(jwtService).extractSubject(mockValidRefreshToken);
        verify(customUserDetailsService).loadUserByUsername(mockSubject);
        verify(jwtService).isTokenValid(mockValidRefreshToken, mockUser);
    }
}
