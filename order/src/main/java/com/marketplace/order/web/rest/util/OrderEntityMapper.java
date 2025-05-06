package com.marketplace.order.web.rest.util;

import com.marketplace.common.util.EntityMapper;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEntityMapper implements EntityMapper<Order, OrderRequest, OrderResponse> {

    @Override
    public OrderResponse mapEntityToResponseDto(Order order) {
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

    @Override
    public Order mapRequestDtoToEntity(OrderRequest orderRequest) {
        return Order.builder()
                .productIds(orderRequest.getProductIds())
                .address(orderRequest.getAddress())
                .status(orderRequest.getStatus())
                .build();
    }

    public List<OrderResponse> mapEntitiesToResponseDtos(List<Order> orders) {
        return orders.stream()
                .map(this::mapEntityToResponseDto)
                .toList();
    }

}
