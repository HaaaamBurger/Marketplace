package com.marketplace.order.service;

import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.dto.OrderRequest;

import java.util.List;
import java.util.Optional;

// TODO think about responsibility of all interfaces (may be Interface Segregation(SOLID - I) not followed)
public interface OrderService {

    Order create(OrderRequest request);

    List<Order> findAll();

    Order findById(String orderId);

    Optional<Order> findByOwnerId();

    Order findByOwnerIdOrCreate();

    Order update(String orderId, OrderUpdateRequest request);

    Order addProductToOrder(String productId);

    void delete(String orderId);

    void removeProductFromOrder(String productId);
}
