package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.List;
import java.util.Set;

public interface ProductManagerService {

    List<Product> findAllByIdIn(Set<String> productIds);

    void decreaseProductsAmountAndSave(List<Product> products);

    ProductsValidationResponse validateProducts(Set<String> productIds);

}
