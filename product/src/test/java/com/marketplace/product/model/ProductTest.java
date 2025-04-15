package com.marketplace.product.model;

import com.marketplace.product.web.model.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenProductIsValid_thenNoViolations() {
        Product product = Product.builder()
                .name("Valid Product Name")
                .description("This is a valid product description.")
                .price(BigDecimal.valueOf(49.99))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty(), "Expected no validation violations");
    }

    @Test
    void whenNameIsBlank_thenValidationFails() {
        Product product = Product.builder()
                .name("  ")
                .description("Valid description")
                .price(BigDecimal.valueOf(49.99))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void whenNameIsTooShort_thenValidationFails() {
        Product product = Product.builder()
                .name("A")
                .description("Valid description")
                .price(BigDecimal.valueOf(49.99))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 2 and 100")));
    }

    @Test
    void whenDescriptionTooShort_thenValidationFails() {
        Product product = Product.builder()
                .name("Valid Name")
                .description("1234")
                .price(BigDecimal.valueOf(49.99))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void whenPriceIsNull_thenValidationFails() {
        Product product = Product.builder()
                .name("Valid Name")
                .description("Valid description")
                .price(null)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void whenPriceIsNegative_thenValidationFails() {
        Product product = Product.builder()
                .name("Valid Name")
                .description("Valid description")
                .price(BigDecimal.valueOf(-5.00))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("greater than 0")));
    }

    @Test
    void whenPriceTooPrecise_thenValidationFails() {
        Product product = Product.builder()
                .name("Valid Name")
                .description("Valid description")
                .price(new BigDecimal("123456789.999"))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("digits")));
    }
}
