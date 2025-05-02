package com.marketplace.product.service;

import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;

import java.util.List;

public interface ProductService {

    Product create(ProductRequest productRequest);

    List<Product> findAll();

    Product findById(String productId);

    Product update(String productId, ProductRequest productRequest);

    void delete(String productId);

}