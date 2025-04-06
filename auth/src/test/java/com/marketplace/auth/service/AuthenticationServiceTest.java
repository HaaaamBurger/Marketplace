package com.marketplace.auth.service;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.rest.dto.AuthRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void shouldReturnMessageOnSignUp() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@gmail.com");
        authRequest.setPassword("testPassword1");

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        String responseString = authenticationService.signUp(authRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(responseString).isNotBlank();
        assertThat(responseString).isEqualTo("User successfully created!!");
    }
}
