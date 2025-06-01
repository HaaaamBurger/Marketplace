package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderSettingsService {

    Optional<Order> findActiveOrderByOwnerId();

    Order findByOwnerIdOrCreate();

    Optional<Order> findOrderByOwnerIdAndStatus(OrderStatus orderStatus);

    List<Order> findOrdersByOwnerIdAndStatusIn(List<OrderStatus> orderStatuses);

    Order addProductToOrder(String productId);

    void removeProductFromOrder(String productId);

    void payForOrder();

}
