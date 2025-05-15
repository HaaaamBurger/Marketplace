package com.marketplace.order.mapper;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEntityMapper {

    public OrderResponse mapOrderToOrderResponseDto(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .ownerId(order.getOwnerId())
                .productIds(order.getProductIds())
                .address(order.getAddress())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderResponse> mapOrdersToOrderResponseDtos(List<Order> orders) {
        return orders.stream()
                .map(this::mapOrderToOrderResponseDto)
                .toList();
    }
}
