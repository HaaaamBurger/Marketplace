package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.List;

public interface ProductService {

    Product create(Product product);

    List<Product> findAll();

    Product findById(String productId);

    Product update(String productId, Product product);

    void delete(String productId);

}
