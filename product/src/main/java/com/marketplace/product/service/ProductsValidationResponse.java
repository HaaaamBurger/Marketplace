package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductsValidationResponse(List<Product> products, boolean hasInvalidProduct) {
}
