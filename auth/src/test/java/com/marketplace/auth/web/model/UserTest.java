package com.marketplace.auth.web.model;

import com.marketplace.auth.web.util.UserDataBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testValidUser() {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testUserWithInvalidEmail() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("testgmail.com")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> matchedViolation = getMatchedViolationByFieldAndMessage(
                violations,
                "email",
                "Must be a valid e-mail address"
        );

        assertThat(matchedViolation).isNotNull();
    }

    @Test
    public void testUserWithInvalidPassword() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password("")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size());

        ConstraintViolation<User> blankPasswordViolation = getMatchedViolationByFieldAndMessage(
                violations,
                "password",
                "Password is required"
        );

        ConstraintViolation<User> invalidLengthPasswordViolation = getMatchedViolationByFieldAndMessage(
                violations,
                "password",
                "Password length must be between 8 to 32 characters"
        );

        assertThat(blankPasswordViolation).isNotNull();
        assertThat(invalidLengthPasswordViolation).isNotNull();
    }

    private ConstraintViolation<User> getMatchedViolationByFieldAndMessage(
            Set<ConstraintViolation<User>> violations,
            String violationField,
            String violationMessage
    ) {
        return violations.stream()
                .filter(userConstraintViolation -> {
                    String field = userConstraintViolation
                            .getPropertyPath()
                            .toString();
                    String message = userConstraintViolation.getMessage();

                    return violationField.equals(field) && violationMessage.equals(message);
                })
                .findFirst()
                .orElseThrow();
    }
}