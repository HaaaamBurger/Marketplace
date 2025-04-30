package com.marketplace.order.util;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class OrderDataBuilder {
    private String id = UUID.randomUUID().toString();
    private String userId = "user-123";
    private List<String> productIds = List.of("product-1", "product-2");
    private String address = "123 Main Street";
    private OrderStatus status = OrderStatus.CREATED;

    public static OrderDataBuilder buildOrderWithAllFields() {
        return new OrderDataBuilder();
    }

    public OrderDataBuilder id(String id) {
        this.id = id;
        return this;
    }

    public OrderDataBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public OrderDataBuilder productIds(List<String> productIds) {
        this.productIds = productIds;
        return this;
    }

    public OrderDataBuilder address(String address) {
        this.address = address;
        return this;
    }

    public OrderDataBuilder status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public Order build() {
        LocalDateTime now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Order order = Order.builder()
                .id(id)
                .userId(userId)
                .productIds(productIds)
                .address(address)
                .status(status)
                .build();

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return order;
    }
}
