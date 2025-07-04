package com.marketplace.order.repository;

import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findOrderByOwnerId(String ownerId);

    @Query(" { 'products._id': { $in: ?0 }, 'status': { $in: ?1 } } ")
    List<Order> findByProductsIdsAndStatuses(Set<String> productsIds, Collection<OrderStatus> statuses);

    Optional<Order> findOrderByOwnerIdAndStatus(String ownerId, OrderStatus status);

    List<Order> findOrdersByOwnerIdAndStatusIn(String ownerId, List<OrderStatus> statuses);
}
