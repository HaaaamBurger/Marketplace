package org.marketplace.security;

import com.marketplace.usercore.config.UserCoreApplicationConfig;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import org.junit.jupiter.api.Test;
import org.marketplace.util.builders.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = UserCoreApplicationConfig.class)
public class AuthenticationUserServiceTest {

    @Autowired
    private AuthenticationUserService authenticationUserService;

    @Test
    public void getAuthenticatedUser_ShouldReturnAuthenticatedUser() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        addAuthenticationToContext(user);

        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void getAuthenticatedUser_ShouldThrowException_WhenAuthenticationIsNull() {
        assertThatThrownBy(() -> authenticationUserService.getAuthenticatedUser())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authentication is unavailable!");
    }

    @Test
    public void getAuthenticatedUser_ShouldThrowException_WhenPrincipleIsNotInstanceOfUser() {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "userDetails",
                null,
                null);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        assertThatThrownBy(() -> authenticationUserService.getAuthenticatedUser())
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("User is not authenticated");
    }

    private void addAuthenticationToContext(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
