package com.marketplace.order.repository;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findOrderByOwnerId(String ownerId);

    Optional<Order> findOrderByOwnerIdAndStatus(String ownerId, OrderStatus status);

    List<Order> findOrdersByOwnerIdAndStatusIn(String ownerId, List<OrderStatus> statuses);
}
