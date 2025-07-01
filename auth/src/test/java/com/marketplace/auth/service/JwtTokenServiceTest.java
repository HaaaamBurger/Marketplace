package com.marketplace.auth.service;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.exception.TokenNotValidException;
import com.marketplace.auth.security.token.JwtService;
import com.marketplace.auth.security.token.TokenPayload;
import com.marketplace.usercore.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthApplicationConfig.class)
public class JwtTokenServiceTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    public void generateTokenPayload_ShouldGenerateTokenPayload() {
        User mockedUser = mock(User.class);
        String mockedAccessToken = "mockedAccessToken";
        String mockedRefreshToken = "mockedRefreshToken";

        when(jwtService.generateAccessToken(mockedUser)).thenReturn(mockedAccessToken);
        when(jwtService.generateRefreshToken(mockedUser)).thenReturn(mockedRefreshToken);

        TokenPayload tokenPayload = jwtTokenService.generateTokenPayload(mockedUser);
        assertThat(tokenPayload).isNotNull();
        assertThat(tokenPayload.getAccessToken()).isEqualTo(mockedAccessToken);
        assertThat(tokenPayload.getRefreshToken()).isEqualTo(mockedRefreshToken);

        verify(jwtService, times(1)).generateAccessToken(mockedUser);
        verify(jwtService, times(1)).generateRefreshToken(mockedUser);
    }

    @Test
    public void generateTokenPayload_ShouldThrowException_WhenUserDetailsIsNull() {
        assertThatThrownBy(() -> jwtTokenService.generateTokenPayload(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User details not present");
    }

    @Test
    public void getUserDetailsIfTokenValidOrThrow_ShouldGetUserDetails() {
        String mockedToken = "mockedToken";
        String mockedSubject = "mockedSubject";
        User mockedUser = mock(User.class);

        when(jwtService.extractSubject(mockedToken)).thenReturn(mockedSubject);
        when(userDetailsService.loadUserByUsername(mockedSubject)).thenReturn(mockedUser);
        when(jwtService.isTokenValid(mockedToken, mockedUser)).thenReturn(true);

        UserDetails userDetails = jwtTokenService.getUserDetailsIfTokenValidOrThrow(mockedToken);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isEqualTo(mockedUser);

        verify(jwtService).extractSubject(mockedToken);
        verify(userDetailsService).loadUserByUsername(mockedSubject);
        verify(jwtService).isTokenValid(mockedToken, mockedUser);
    }

    @Test
    public void getUserDetailsIfTokenValidOrThrow_ShouldThrowException_WhenTokenIsNotValid() {
        String mockedToken = "mockedToken";
        String mockedSubject = "mockedSubject";
        User mockedUser = mock(User.class);

        when(jwtService.extractSubject(mockedToken)).thenReturn(mockedSubject);
        when(userDetailsService.loadUserByUsername(mockedSubject)).thenReturn(mockedUser);
        when(jwtService.isTokenValid(mockedToken, mockedUser)).thenReturn(false);

        assertThatThrownBy(() -> jwtTokenService.getUserDetailsIfTokenValidOrThrow(mockedToken))
                .isInstanceOf(TokenNotValidException.class)
                .hasMessage("Token not valid!");
    }

    @Test
    public void getUserDetailsIfTokenValidOrThrow_ShouldThrowException_WhenTokenIsNull() {
        assertThatThrownBy(() -> jwtTokenService.getUserDetailsIfTokenValidOrThrow(null))
                .isInstanceOf(TokenNotValidException.class)
                .hasMessage("Token not valid!");
    }
}
