package com.marketplace.main.config;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DataInitializerTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    public void init_ShouldCreateAdminUser() {
        String mockedEmail = "mockedEmail";
        String mockedPassword = "mockedPassword";
        String mockedEncodedPassword = "mockedEncodedPassword";
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByEmail(mockedEmail)).thenReturn(false);
        when(passwordEncoder.encode(mockedPassword)).thenReturn(mockedEncodedPassword);
        when(userRepository.save(userArgumentCaptor.capture()));

        dataInitializer.run();

        assertThat(userArgumentCaptor.getValue()).isNotNull();
        assertThat(userArgumentCaptor.getValue().getEmail()).isEqualTo(mockedEmail);
    }

}
