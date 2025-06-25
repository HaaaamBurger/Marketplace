package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

public interface ProductValidationService {

    void validateProductOrThrow(Product product);

     boolean isNotValidProduct(Product product);

}
