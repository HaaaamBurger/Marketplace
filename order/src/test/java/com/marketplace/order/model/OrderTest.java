package com.marketplace.order.model;


import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.web.model.Order;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testValidOrder() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testOrderWithNullOwnerId() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(null)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        ConstraintViolation<Order> matchedViolation = getMatchedViolationByField(violations, "ownerId");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("ownerId");
        assertThat(matchedViolation.getMessage()).isEqualTo("Owner ID is required");
    }

    @Test
    public void testOrderWithEmptyProductIds() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(List.of())
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        ConstraintViolation<Order> matchedViolation = getMatchedViolationByField(violations, "productIds");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("productIds");
        assertThat(matchedViolation.getMessage()).isEqualTo("Order must contain at least 1 product and maximum 50");
    }

    @Test
    public void testOrderWithNullStatus() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(null)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        ConstraintViolation<Order> matchedViolation = getMatchedViolationByField(violations, "status");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("status");
        assertThat(matchedViolation.getMessage()).isEqualTo("Status is required");
    }

    private ConstraintViolation<Order> getMatchedViolationByField(Set<ConstraintViolation<Order>> violations, String field) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(field))
                .findFirst()
                .orElse(null);
    }
}
