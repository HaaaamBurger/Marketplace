package com.marketplace.main.util.builder;

import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.dto.OrderRequest;

import java.util.Set;
import java.util.UUID;

public class OrderRequestDataBuilder {

    public static OrderRequest.OrderRequestBuilder buildOrderWithAllFields() {
        return OrderRequest.builder()
                .productIds(Set.of(String.valueOf(UUID.randomUUID())))
                .status(OrderStatus.CREATED)
                .address(String.valueOf(UUID.randomUUID()));
    }

}
