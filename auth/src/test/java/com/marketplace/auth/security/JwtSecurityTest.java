package com.marketplace.auth.security;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.util.UserDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class JwtSecurityTest {

    @Autowired
    private JwtService jwtService;


    @Test
    public void generateAccessToken_shouldGenerateValidAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user);

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(accessToken, user)).isTrue();
    }

    @Test
    public void generateRefreshToken_shouldGenerateValidRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.extractSubject(refreshToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(refreshToken, user)).isTrue();
    }

    @Test
    public void generateAccessToken_shouldGenerateInvalidAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        User fakeUser = UserDataBuilder.buildUserWithAllFields().email("test1@gmail.com").build();

        String accessToken = jwtService.generateAccessToken(fakeUser);

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(fakeUser.getUsername());
        assertThat(jwtService.isTokenValid(accessToken, user)).isFalse();
    }

    @Test
    public void generateRefreshToken_shouldGenerateInvalidRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        User fakeUser = UserDataBuilder.buildUserWithAllFields().email("test1@gmail.com").build();

        String refreshToken = jwtService.generateRefreshToken(fakeUser);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.extractSubject(refreshToken)).isEqualTo(fakeUser.getUsername());
        assertThat(jwtService.isTokenValid(refreshToken, user)).isFalse();
    }

    @Test
    public void generateRefreshTokenWithExpiration_shouldGenerateExpiredRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateRefreshTokenWithExpiration(user, 0);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenExpired(refreshToken)).isTrue();
    }
}
