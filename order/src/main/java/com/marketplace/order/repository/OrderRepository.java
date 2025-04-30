package com.marketplace.order.repository;

import com.marketplace.order.web.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order,String> {
}
