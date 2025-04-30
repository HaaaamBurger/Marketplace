package com.marketplace.order.service;

import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders();

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(String id);

    OrderResponse updateOrder(String id, OrderRequest request);

    void deleteOrder(String id);
}
