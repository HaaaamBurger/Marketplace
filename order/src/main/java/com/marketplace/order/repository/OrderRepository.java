package com.marketplace.order.repository;

import com.marketplace.order.web.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order,String> {

    Optional<Order> findOrderByUserId(String userId);

}
