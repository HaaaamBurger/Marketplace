package com.marketplace.order.service;

import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.dto.OrderRequest;

import java.util.List;

public interface OrderCrudService {

    Order create(OrderRequest request);

    List<Order> findAll();

    Order findById(String orderId);

    Order update(String orderId, OrderUpdateRequest request);

    void delete(String orderId);
}
