package com.marketplace.product.model;

import com.marketplace.product.util.ProductDataBuilder;
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
    public static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void whenProductIsValid_thenNoViolations() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void whenNameIsBlank_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .name(" ")
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    public void whenNameIsTooShort_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .name("t")
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 2 and 100")));
    }

    @Test
    public void whenDescriptionTooShort_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .description("test")
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    public void whenPriceIsNull_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .price(null)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    public void whenPriceIsNegative_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .price(BigDecimal.valueOf(-5.00))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("greater or equal to 0")));
    }

    @Test
    public void whenPriceTooPrecise_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .price(new BigDecimal("123456789.999"))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("digits")));
    }

    @Test
    public void whenAmountIsNull_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(null)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount is required")));
    }

    @Test
    public void whenAmountIsNegative_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(-1)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Amount cannot be negative value")));
    }

    @Test
    public void whenActiveIsNull_thenValidationFails() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .active(null)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Active is required")));
    }


    @Test
    public void decreaseAmount_ShouldDecreaseAmount() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(1)
                .build();

        boolean hasDecreasedAmount = product.decreaseAmount();
        assertTrue(hasDecreasedAmount);
        assertEquals(0, (int) product.getAmount());
    }

    @Test
    public void decreaseAmount_ShouldNotDecreaseAmount() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(0)
                .build();

        boolean hasDecreasedAmount = product.decreaseAmount();
        assertFalse(hasDecreasedAmount);
        assertEquals(0, (int) product.getAmount());
    }

}
