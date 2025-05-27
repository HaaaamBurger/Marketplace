package com.marketplace.product.repository;

import com.marketplace.product.web.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product,String> {

    Optional<Product> findProductByOwnerId(String ownerId);

}
