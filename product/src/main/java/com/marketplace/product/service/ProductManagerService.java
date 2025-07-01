package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductManagerService {

    Optional<Product> requireProductById(String productId);

    List<Product> findAllByIdIn(Set<String> productIds);

    void decreaseProductsAmountAndSave(Set<Product> products);

}
