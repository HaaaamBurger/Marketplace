package org.marketplace.service;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.usercore.config.UserCoreApplicationConfig;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.service.UserBusinessService;
import org.junit.jupiter.api.Test;
import org.marketplace.util.builder.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserCoreApplicationConfig.class)
public class UserBusinessServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private UserBusinessService userBusinessService;

    @Test
    public void existsByEmail_ShouldReturnTrue_WhenUserExists() {
        String email = String.valueOf(UUID.randomUUID());

        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean existsByEmail = userBusinessService.existsByEmail(email);

        assertThat(existsByEmail).isTrue();
    }
    @Test
    public void existsByEmail_ShouldReturnFalse_WhenUserNotExists() {
        String email = String.valueOf(UUID.randomUUID());

        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean existsByEmail = userBusinessService.existsByEmail(email);

        assertThat(existsByEmail).isFalse();
    }

    @Test
    public void throwIfUserExistsByEmail_ShouldDoNothing_WhenUserNotExists() {
        String email = String.valueOf(UUID.randomUUID());

        when(userRepository.existsByEmail(email)).thenReturn(false);

        userBusinessService.throwIfUserExistsByEmail(email);
    }

    @Test
    public void throwIfUserExistsByEmail_ShouldThrowException_WhenUserExists() {
        String email = String.valueOf(UUID.randomUUID());

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userBusinessService.throwIfUserExistsByEmail(email)).isInstanceOf(EntityExistsException.class);
    }

    @Test
    public void throwIfUserNotFoundById_ShouldDoNothing_WhenUserExistsOrGet() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User responseUser = userBusinessService.throwIfUserNotFoundByIdOrGet(user.getId());

        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getId()).isEqualTo(user.getId());
    }

    @Test
    public void throwIfUserNotFoundById_ShouldThrowException_WhenUserNotExistsOrGet() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBusinessService.throwIfUserNotFoundByIdOrGet(user.getId())).isInstanceOf(EntityNotFoundException.class);
    }
}
