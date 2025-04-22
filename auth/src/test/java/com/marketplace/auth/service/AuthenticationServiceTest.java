package com.marketplace.auth.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.service.impl.AuthenticationServiceImpl;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import com.marketplace.auth.web.util.AuthRequestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void shouldReturnTokenOnSignIn() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        UserDetails mockUserDetails = mock(UserDetails.class);
        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";
        String encodedPassword = "mockEncodedPassword";

        when(mockUserDetails.getPassword()).thenReturn(encodedPassword);
        when(userDetailsService.loadUserByUsername(authRequest.getEmail())).thenReturn(mockUserDetails);
        when(passwordEncoder.matches(authRequest.getPassword(), encodedPassword)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUserDetails)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationService.signIn(authRequest);

        assertNotNull(authResponse);
        assertThat(mockAccessToken).isEqualTo(authResponse.getAccessToken());
        assertThat(mockRefreshToken).isEqualTo(authResponse.getRefreshToken());
    }

    @Test
    public void shouldThrowExceptionOnSignInWhenUserNotFound() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userDetailsService.loadUserByUsername(authRequest.getEmail())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> authenticationService.signIn(authRequest));
    }

    @Test
    public void shouldReturnMessageSignUp() {
        String encodedPassword = "encodedPassword";
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn(encodedPassword);
        String responseString = authenticationService.signUp(authRequest);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(authRequest.getEmail());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);
        assertThat(responseString).isNotBlank();
        assertThat(responseString).isEqualTo("User successfully created!");
    }

    @Test
    public void shouldThrowExceptionIfUserAlreadyExists() {
        User mockUser = mock(User.class);
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> authenticationService.signUp(authRequest))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User already exists!");
    }

    @Test
    public void shouldReturnNewPairOfTokensWhenOnRefreshToken() {
        String validRefreshToken = "validRefreshToken";
        String mockSubject = "mockSubject";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";
        UserDetails mockUserDetails = mock(UserDetails.class);

        AuthRefreshRequest authRefreshRequest = AuthRefreshRequest.builder().refreshToken(validRefreshToken).build();

        when(userDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUserDetails);
        when(jwtService.extractSubject(validRefreshToken)).thenReturn(mockSubject);
        when(jwtService.isTokenValid(validRefreshToken, mockUserDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(mockUserDetails)).thenReturn(refreshToken);

        AuthResponse authResponse = authenticationService.refreshToken(authRefreshRequest);

        assertNotNull(authResponse);
        assertThat(authResponse.getAccessToken()).isEqualTo(accessToken);
        assertThat(authResponse.getRefreshToken()).isEqualTo(refreshToken);
    }
}