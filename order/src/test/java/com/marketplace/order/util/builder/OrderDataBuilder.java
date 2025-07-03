package com.marketplace.order.util.builder;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.util.Set;
import java.util.UUID;

public class OrderDataBuilder {

    public static Order.OrderBuilder buildOrderWithAllFields() {
        return Order.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .ownerId(String.valueOf(UUID.randomUUID()))
                .products(Set.of(ProductDataBuilder.buildProductWithAllFields().build()))
                .address(String.valueOf(UUID.randomUUID()))
                .status(OrderStatus.CREATED);
    }

}
