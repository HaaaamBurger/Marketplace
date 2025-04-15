package com.marketplace.product.repository;

import com.marketplace.product.web.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}
