package com.marketplace.product.service;

import com.marketplace.product.exception.ProductNotAvailableException;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultProductValidationService implements ProductValidationService {

    @Override
    public void validateProductOrThrow(Product product) {
        if (isNotValidProduct(product)) {
            throw new ProductNotAvailableException("This product is not available");
        }
    }

    @Override
    public boolean isNotValidProduct(Product product) {
        return product.getAmount() == 0 || !product.getActive();
    }

    @Override
    public boolean validateProducts(Set<Product> products) {
        return products.stream().anyMatch(this::isNotValidProduct);
    }

}
