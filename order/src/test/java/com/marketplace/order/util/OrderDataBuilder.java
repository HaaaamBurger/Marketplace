package com.marketplace.order.util;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public class OrderDataBuilder {

    public static Order.OrderBuilder buildOrderWithAllFields() {
        return Order.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .ownerId(String.valueOf(UUID.randomUUID()))
                .productIds(List.of(String.valueOf(UUID.randomUUID())))
                .address(String.valueOf(UUID.randomUUID()))
                .status(OrderStatus.CREATED);
    }

}
