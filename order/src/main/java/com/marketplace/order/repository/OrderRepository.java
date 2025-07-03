package com.marketplace.order.repository;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.web.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findOrderByOwnerId(String ownerId);

    List<Order> findByProductsContainingAndStatusIn(Set<Product> products, Collection<OrderStatus> statuses);

    Optional<Order> findOrderByOwnerIdAndStatus(String ownerId, OrderStatus status);

    List<Order> findOrdersByOwnerIdAndStatusIn(String ownerId, List<OrderStatus> statuses);
}
