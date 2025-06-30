package org.marketplace.service;

import com.marketplace.usercore.config.UserCoreApplicationConfig;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.service.DefaultUserValidationService;
import org.junit.jupiter.api.Test;
import org.marketplace.util.builder.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = UserCoreApplicationConfig.class)
public class DefaultUserValidationServiceTest {

    @Autowired
    private DefaultUserValidationService defaultUserValidationService;

    @Test
    public void validateEntityOwnerOrAdmin_ShouldReturnTrue_WhenUserOwner() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        boolean validateEntityOwnerOrAdmin = defaultUserValidationService.validateEntityOwnerOrAdmin(user, user.getId());

        assertThat(validateEntityOwnerOrAdmin).isTrue();
    }

    @Test
    public void validateEntityOwnerOrAdmin_ShouldReturnTrue_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        String userId = String.valueOf(UUID.randomUUID());

        boolean validateEntityOwnerOrAdmin = defaultUserValidationService.validateEntityOwnerOrAdmin(user, userId);

        assertThat(validateEntityOwnerOrAdmin).isTrue();
    }

    @Test
    public void validateEntityOwnerOrAdmin_ShouldReturnFalse_WhenUserNotOwnerAndNotAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        String userId = String.valueOf(UUID.randomUUID());

        boolean validateEntityOwnerOrAdmin = defaultUserValidationService.validateEntityOwnerOrAdmin(user, userId);

        assertThat(validateEntityOwnerOrAdmin).isFalse();
    }

}
