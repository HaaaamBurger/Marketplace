package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;

import java.util.Optional;

public interface OrderSettingsService {

    Optional<Order> findByOwnerId();

    Order findByOwnerIdOrCreate();

    Order addProductToOrder(String productId);

    void removeProductFromOrder(String productId);

}
