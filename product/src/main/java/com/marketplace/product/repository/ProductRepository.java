package com.marketplace.product.repository;

import com.marketplace.product.web.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends MongoRepository<Product,String> {

    Optional<Product> findProductByOwnerId(String ownerId);

    List<Product> findAllByIdIn(Set<String> productIds);

}
