package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.Set;

public interface ProductValidationService {

    void validateProductOrThrow(Product product);

    boolean isNotValidProduct(Product product);

    boolean validateProducts(Set<Product> products);

}
