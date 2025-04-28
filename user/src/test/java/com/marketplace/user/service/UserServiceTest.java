package com.marketplace.user.service;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.common.model.UserStatus;
import com.marketplace.user.util.UserDataBuilder;
import com.marketplace.user.web.dto.UserCreateRequest;
import com.marketplace.user.web.dto.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Test
    public void create_shouldCreateUser() {
        String mockEmail = "mockEmail";
        String mockPassword = "mockPassword";
        String mockEncodedPassword = "mockEncodedPassword";
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email(mockEmail)
                .role(UserRole.USER)
                .password(mockPassword)
                .build();

        when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(mockPassword)).thenReturn(mockEncodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User responseUser = userService.create(userCreateRequest);

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmail()).isEqualTo(mockEmail);
        assertThat(responseUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(responseUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(responseUser.getPassword()).isEqualTo(mockEncodedPassword);

        verify(userRepository).findByEmail(mockEmail);
        verify(passwordEncoder).encode(mockPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void create_shouldThrowException_WhenUserExists() {
        User mockUser = mock(User.class);
        String mockEmail = "mockEmail";
        String mockPassword = "mockPassword";
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email(mockEmail)
                .role(UserRole.USER)
                .password(mockPassword)
                .build();

        when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> userService.create(userCreateRequest))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User already exists!");

        verify(userRepository).findByEmail(mockEmail);
    }

    @Test
    public void findAll_shouldReturnListOfAllUsers() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("test@gmail.com")
                .build();
        User user1 = UserDataBuilder.buildUserWithAllFields()
                .email("test1@gmail.com")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user, user1));

        List<User> responseUsers = userService.findAll();
        assertThat(responseUsers).isNotNull();
        assertThat(responseUsers.size()).isEqualTo(2);
        assertThat(responseUsers.get(0).getEmail()).isEqualTo("test@gmail.com");
        assertThat(responseUsers.get(1).getEmail()).isEqualTo("test1@gmail.com");

        verify(userRepository).findAll();
    }

    @Test
    public void findById_shouldReturnUserById() {
        String userId = String.valueOf(UUID.randomUUID());
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User responseUser = userService.findById(userId);
        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getId()).isEqualTo(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void findById_shouldThrowException() {
        String userId = String.valueOf(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownByFindById(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void update_shouldReturnUpdatedUser() {
        String userId = String.valueOf(UUID.randomUUID());
        User user = UserDataBuilder.buildUserWithAllFields().build();
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .email("test1@gmail.com")
                .role(UserRole.ADMIN)
                .password("testPassword2")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User responseUser = userService.update(userId, userUpdateRequest);
        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmail()).isEqualTo(userUpdateRequest.getEmail());
        assertThat(responseUser.getRole()).isEqualTo(userUpdateRequest.getRole());

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    public void update_shouldThrowException() {
        String userId = String.valueOf(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownByFindById(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void updateStatus_shouldUpdateStatus() {
        String userId = String.valueOf(UUID.randomUUID());
        User user = UserDataBuilder.buildUserWithAllFields().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateStatus(userId, UserStatus.BLOCKED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);

        verify(userRepository).save(user);
    }

    @Test
    public void updateStatus_shouldThrowException() {
        String userId = String.valueOf(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownByFindById(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void delete_shouldDeleteUserById() {
        String userId = String.valueOf(UUID.randomUUID());
        User user = UserDataBuilder.buildUserWithAllFields().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);

        userService.delete(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> optionalUser = userRepository.findById(userId);
        assertThat(optionalUser).isNotPresent();

        verify(userRepository, times(2)).findById(userId);
    }

    @Test
    public void delete_shouldThrowException() {
        String userId = String.valueOf(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownByFindById(userId);

        verify(userRepository).findById(userId);
    }

    private void assertThatThrownByFindById(String userId) {
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found by id: " + userId);
    }
}
