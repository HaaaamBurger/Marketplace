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

        assertTrue(violations.isEmpty(), "Order should be valid");
    }

    @Test
    public void testOrderWithNullUserId() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .userId(null)
                .build();

        Set<ConstraintViolation<Order>> violations = validator.validate(order);

        ConstraintViolation<Order> matchedViolation = getMatchedViolationByField(violations, "userId");

        assertThat(matchedViolation).isNotNull();
        assertThat(matchedViolation.getPropertyPath().toString()).isEqualTo("userId");
         assertThat(matchedViolation.getMessage()).isEqualTo("User ID cannot be null");
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
         assertThat(matchedViolation.getMessage()).isEqualTo("Order must contain at least one product");
    }

    private ConstraintViolation<Order> getMatchedViolationByField(Set<ConstraintViolation<Order>> violations, String field) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(field))
                .findFirst()
                .orElse(null);
    }
}
