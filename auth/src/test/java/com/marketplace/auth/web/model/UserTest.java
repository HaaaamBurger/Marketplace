package com.marketplace.auth.web.model;

import com.marketplace.auth.web.util.UserDataBuilder;
import com.marketplace.usercore.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void whenUserIsValid_thenNoViolations() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void whenEmailIsBlank_thenValidationFails() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    public void whenEmailIsInvalid_thenValidationFails() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("testgmail.com")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Must be a valid e-mail address")));
    }


    @Test
    public void whenPasswordIsInvalid_thenValidationFails() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password("")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size());

        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password length must be between 8 to 32 characters")));
    }

}