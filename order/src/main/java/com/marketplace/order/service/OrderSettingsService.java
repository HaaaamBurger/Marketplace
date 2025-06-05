package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderSettingsService {

    Order addProductToOrder(String productId);

    Optional<Order> findOrderByOwnerIdAndStatus(OrderStatus orderStatus);

    List<Order> findOrdersByOwnerIdAndStatusIn(List<OrderStatus> orderStatuses);

    void removeProductFromOrder(String productId);

    void payForOrder();

}
