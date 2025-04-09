package com.marketplace.auth.service;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.rest.dto.AuthRefreshRequest;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import com.marketplace.auth.web.rest.dto.AuthResponse;
import com.marketplace.auth.web.util.AuthRequestDataBuilder;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

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
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void shouldReturnTokenOnSignIn() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        UserDetails mockUserDetails = mock(UserDetails.class);
        User mockUser = mock(User.class);

        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";


        when(userDetailsService.loadUserByUsername(authRequest.getEmail())).thenReturn(mockUserDetails);
        when(jwtService.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponse authResponse = authenticationService.signIn(authRequest);

        assertNotNull(authResponse);
        assertThat(mockAccessToken).isEqualTo(authResponse.getAccessToken());
        assertThat(mockRefreshToken).isEqualTo(authResponse.getRefreshToken());
    }

    @Test
    public void shouldThrowExceptionOnSignInWhenUserNotFound() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        when(userDetailsService.loadUserByUsername(authRequest.getEmail())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            authenticationService.signIn(authRequest);
        });
    }

    @Test
    public void shouldReturnMessageOnSignUp() {
        AuthRequest authRequest = AuthRequestDataBuilder.withAllFields().build();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User capturedUser = userCaptor.getValue();

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());
        String responseString = authenticationService.signUp(authRequest);

        verify(userRepository).save(userCaptor.capture());

        assertThat(capturedUser.getEmail()).isEqualTo(authRequest.getEmail());
        assertThat(capturedUser.getPassword()).isEqualTo(authRequest.getPassword());
        assertThat(responseString).isNotBlank();
        assertThat(responseString).isEqualTo("User successfully created!!");
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

        UserDetails mockUserDetails = mock(UserDetails.class);

        AuthRefreshRequest authRefreshRequest = new AuthRefreshRequest();
        authRefreshRequest.setRefreshToken(validRefreshToken);

        when(userDetailsService.loadUserByUsername(mockSubject)).thenReturn(mockUserDetails);
        when(jwtService.extractSubject(validRefreshToken)).thenReturn(mockSubject);
        when(jwtService.isTokenValid(validRefreshToken, mockUserDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(mockUserDetails)).thenReturn("newRefreshToken");

        AuthResponse authResponse = authenticationService.refreshToken(authRefreshRequest);

        assertNotNull(authResponse);
        assertThat(authResponse.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(authResponse.getRefreshToken()).isEqualTo("newRefreshToken");
    }
}