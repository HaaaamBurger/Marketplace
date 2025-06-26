package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.web.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderManagerService {

    Order addProductToOrder(String productId);

    Order findOrderOrThrow(String orderId);

    Optional<Order> findOrderByOwnerIdAndStatus(OrderStatus orderStatus);

    List<Order> findOrdersByOwnerIdAndStatusIn(List<OrderStatus> orderStatuses);

    void removeProductFromOrder(String productId);

    void payForOrder();

    BigDecimal calculateTotalSum(List<Product> products);
}
