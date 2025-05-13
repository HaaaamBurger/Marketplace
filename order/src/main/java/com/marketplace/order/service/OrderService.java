package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.dto.OrderRequest;

import java.util.List;

public interface OrderService {

    Order create(OrderRequest request);

    List<Order> findAll();

    Order findById(String orderId);

    Order findByOwnerIdOrCreate();

    Order update(String orderId, OrderRequest request);

    Order addProductToOrder(String productId);

    void delete(String orderId);
}
