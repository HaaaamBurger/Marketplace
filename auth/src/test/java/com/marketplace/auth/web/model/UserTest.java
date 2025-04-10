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

        assertTrue(violations.isEmpty(), "User should be valid");
    }

    @Test
    public void testUserWithInvalidEmail() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .email("testgmail.com")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> matchedViolation = getMatchedViolationByField(violations, "email");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(matchedViolation.getMessage()).isEqualTo("Must be a valid e-mail address");
    }

    @Test
    public void testUserWithInvalidPassword() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .password("")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> matchedViolation = getMatchedViolationByField(violations, "password");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("password");
        assertThat(matchedViolation.getMessage()).isEqualTo("Password cannot be blank");
    }

    private ConstraintViolation<User> getMatchedViolationByField(Set<ConstraintViolation<User>> violations, String field) {
        return violations.stream()
                .filter(userConstraintViolation -> userConstraintViolation
                        .getPropertyPath()
                        .toString()
                        .equals(field))
                .findFirst()
                .orElseThrow();
    }
}