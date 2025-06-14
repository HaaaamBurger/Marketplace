package com.marketplace.auth.security;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.web.util.builders.UserDataBuilder;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthApplicationConfig.class)
public class CustomUserDetailsServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsername_ShouldReturnUserDetails() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String userEmail = "test@gmail.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(userEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User does not exist!");

    }

    @Test
    public void loadUserByUsername_ShouldThrowException_WhenUsernameIsNull() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User does not exist!");
    }

}
