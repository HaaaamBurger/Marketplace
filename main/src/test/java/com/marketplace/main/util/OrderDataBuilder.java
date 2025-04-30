package com.marketplace.main.util;

import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import java.util.Arrays;
import java.util.List;

public class OrderDataBuilder {

    // Створення стандартного OrderRequest для контролера
    public static OrderRequest.OrderRequestBuilder defaultOrderRequest() {
        return OrderRequest.builder()
                .productIds(Arrays.asList("productId1", "productId2"))
                .address("123 Main St, City, Country")
                .status(OrderStatus.CREATED);  // Використовуємо OrderStatus enum
    }

    // Створення стандартного Order для контролера
    public static Order.OrderBuilder defaultOrder(String userId) {
        return Order.builder()
                .userId(userId)
                .productIds(Arrays.asList("productId1", "productId2"))
                .address("123 Main St, City, Country")
                .status(OrderStatus.CREATED);  // Використовуємо OrderStatus enum
    }

    // Створення Order з конкретними параметрами
    public static Order.OrderBuilder customOrder(String userId, List<String> productIds, String address, OrderStatus status) {
        return Order.builder()
                .userId(userId)
                .productIds(productIds)
                .address(address)
                .status(status);  // Використовуємо OrderStatus enum
    }
}
