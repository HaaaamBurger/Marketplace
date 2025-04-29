package com.marketplace.product.service;

import com.marketplace.product.web.dto.ProductCreateRequest;
import com.marketplace.product.web.model.Product;

import java.util.List;

public interface ProductService {

    Product create(ProductCreateRequest productCreateRequest);

    List<Product> findAll();

    Product findById(String productId);

    Product update(String productId, Product product);

    void delete(String productId);

}
