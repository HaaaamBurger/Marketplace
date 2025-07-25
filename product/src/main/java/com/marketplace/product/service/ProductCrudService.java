package com.marketplace.product.service;

import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;

import java.util.List;
import java.util.Set;

public interface ProductCrudService {

    Product create(ProductRequest productRequest);

    List<Product> findAll();

    Product getById(String productId);

    Product update(String productId, ProductRequest productRequest);

    void delete(String productId);

}