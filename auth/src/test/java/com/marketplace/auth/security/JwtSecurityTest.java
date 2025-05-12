package com.marketplace.auth.security;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.security.service.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.util.UserDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static com.marketplace.auth.security.service.JwtService.ROLES_CLAIM;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = AuthApplicationConfig.class)
public class JwtSecurityTest {

    @Autowired
    private JwtService jwtService;

    @Test
    public void isTokenValid_ShouldReturnTrue_WhenAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user);

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.isTokenValid(accessToken, user)).isTrue();
    }

    @Test
    public void isTokenValid_ShouldReturnFalse_WhenAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user);
        user.setEmail("test1@gmail.com");

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.isTokenValid(accessToken, user)).isFalse();
    }

    @Test
    public void isTokenExpired_ShouldReturnTrue_WhenAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateAccessTokenWithExpiration(user, 0);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenExpired(refreshToken)).isTrue();
    }

    @Test
    public void isTokenExpired_ShouldReturnFalse_WhenRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenExpired(refreshToken)).isFalse();
    }

    @Test
    public void isTokenExpired_ShouldReturnTrue_WhenRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateRefreshTokenWithExpiration(user, 0);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenExpired(refreshToken)).isTrue();
    }

    @Test
    public void isTokenExpired_ShouldReturnFalse_WhenAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateAccessToken(user);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenExpired(refreshToken)).isFalse();
    }

    @Test
    public void extractClaim_shouldReturnValue() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user, Map.of(ROLES_CLAIM, "ADMIN"));

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(accessToken, user)).isTrue();

        Object role = jwtService.extractClaim(accessToken, ROLES_CLAIM);

        assertThat(role).isNotNull();
        assertThat(role).isInstanceOf(String.class);
        assertThat((String) role).isEqualTo("ADMIN");
    }

    @Test
    public void extractClaim_shouldReturnNullValue() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user, Map.of(ROLES_CLAIM, "ADMIN"));

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(accessToken, user)).isTrue();

        Object role = jwtService.extractClaim(accessToken, "status");

        assertThat(role).isNull();
    }

    @Test
    public void extractSubject_shouldReturnSubject() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user);

        String subject = jwtService.extractSubject(accessToken);

        assertThat(subject).isNotBlank();
        assertThat(subject).isEqualTo(user.getEmail());
    }

    @Test
    public void generateAccessToken_shouldGenerateValidAccessToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String accessToken = jwtService.generateAccessToken(user);

        assertThat(accessToken).isNotBlank();
        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(accessToken, user)).isTrue();
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
    public void generateRefreshToken_shouldGenerateValidRefreshToken() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.extractSubject(refreshToken)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(refreshToken, user)).isTrue();
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