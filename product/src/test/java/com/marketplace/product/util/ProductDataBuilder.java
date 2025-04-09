package com.marketplace.product.util;

import com.marketplace.product.model.Product;

import java.math.BigDecimal;


public class ProductDataBuilder {

    public static Product.ProductBuilder buildProductWithAllFields() {
        return Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99));
    }
}
