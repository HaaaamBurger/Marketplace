package com.marketplace.product.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Product createValidProduct() {
        return new Product(
                UUID.randomUUID(),
                "Valid Product Name",
                "This is a valid product description.",
                BigDecimal.valueOf(49.99)
        );
    }

    @Test
    void whenProductIsValid_thenNoViolations() {
        Product product = createValidProduct();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertTrue(violations.isEmpty(), "Expected no validation violations");
    }

    @Test
    void whenNameIsBlank_thenValidationFails() {
        Product product = createValidProduct();
        product.setName("  ");

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void whenNameIsTooShort_thenValidationFails() {
        Product product = createValidProduct();
        product.setName("A");

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 2 and 100")));
    }

    @Test
    void whenDescriptionTooShort_thenValidationFails() {
        Product product = createValidProduct();
        product.setDescription("1234");

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void whenPriceIsNull_thenValidationFails() {
        Product product = createValidProduct();
        product.setPrice(null);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void whenPriceIsNegative_thenValidationFails() {
        Product product = createValidProduct();
        product.setPrice(BigDecimal.valueOf(-5.00));

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("greater than 0")));
    }

    @Test
    void whenPriceTooPrecise_thenValidationFails() {
        Product product = createValidProduct();
        product.setPrice(new BigDecimal("123456789.999"));

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("digits")));
    }

    @Test
    void whenNameHasInvalidCharacters_thenValidationFails() {
        Product product = createValidProduct();
        product.setName("Invalid@Name!");

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("invalid characters")));
    }
}
