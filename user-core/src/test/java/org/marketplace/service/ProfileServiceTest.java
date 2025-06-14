package org.marketplace.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.usercore.config.UserCoreApplicationConfig;
import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.ProfileService;
import com.marketplace.usercore.service.UserSettingsService;
import org.junit.jupiter.api.Test;
import org.marketplace.util.builders.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserCoreApplicationConfig.class)
public class ProfileServiceTest {

    @MockitoBean
    private UserSettingsService userSettingsService;

    @MockitoBean
    private AuthenticationUserService authenticationUserService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ProfileService profileService;

    @Test
    public void update_ShouldUpdateProfile() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(String.valueOf(UUID.randomUUID()))
                .build();

        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .email("test123@gmail.com")
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(userSettingsService.throwIfUserNotFoundById(user.getId())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = profileService.update(user.getId(), profileUpdateRequest);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(profileUpdateRequest.getEmail());

        verify(authenticationUserService).getAuthenticatedUser();
        verify(userSettingsService).throwIfUserNotFoundById(user.getId());
        verify(userRepository).save(user);
    }

    @Test
    public void update_ShouldThrowException_WhenAccessDenied() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(String.valueOf(UUID.randomUUID()))
                .build();
        String user1 = String.valueOf(UUID.randomUUID());

        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .email("test123@gmail.com")
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);

        assertThatThrownBy(() -> profileService.update(user1, profileUpdateRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied!");

        verify(authenticationUserService).getAuthenticatedUser();
    }

    @Test
    public void update_ShouldNotSaveUserAndReturn_WhenEmailIsTheSameAsCurrent() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(String.valueOf(UUID.randomUUID()))
                .build();

        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .email(user.getEmail())
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(userSettingsService.throwIfUserNotFoundById(user.getId())).thenReturn(user);

        User updatedUser = profileService.update(user.getId(), profileUpdateRequest);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(user.getEmail());

        verify(authenticationUserService).getAuthenticatedUser();
        verify(userSettingsService).throwIfUserNotFoundById(user.getId());
        verify(userRepository, never()).save(user);
    }

    @Test
    public void update_ShouldNotSaveUserAndReturn_WhenEmailIsTheSameAsExistingOne() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .id(String.valueOf(UUID.randomUUID()))
                .build();

        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .email("test123@gmail.com")
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(userSettingsService.throwIfUserNotFoundById(user.getId())).thenReturn(user);
        doThrow(new EntityExistsException("User with this email already exists")).when(userSettingsService).throwIfUserWithSameEmailExists(profileUpdateRequest.getEmail());

        assertThatThrownBy(() -> profileService.update(user.getId(), profileUpdateRequest))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User with this email already exists");

        verify(authenticationUserService).getAuthenticatedUser();
        verify(userSettingsService).throwIfUserNotFoundById(user.getId());
        verify(userRepository, never()).save(any());
    }
}
