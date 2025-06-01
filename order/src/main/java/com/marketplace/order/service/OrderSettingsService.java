package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderSettingsService {

    Optional<Order> findByOwnerId();

    Order findByOwnerIdOrCreate();

    Order addProductToOrder(String productId);

    void removeProductFromOrder(String productId);

    List<Order> findOrdersByOwnerIdAndStatusIn(List<OrderStatus> orderStatus);

    void payForOrder();

}
