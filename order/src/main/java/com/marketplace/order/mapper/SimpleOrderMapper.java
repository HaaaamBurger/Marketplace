package com.marketplace.order.mapper;

import com.marketplace.order.web.dto.OrderResponse;
import com.marketplace.order.web.model.Order;

import java.util.List;

public interface SimpleOrderMapper {

    OrderResponse mapOrderToOrderResponseDto(Order order);

    List<OrderResponse> mapOrdersToOrderResponseDtos(List<Order> orders);

}
