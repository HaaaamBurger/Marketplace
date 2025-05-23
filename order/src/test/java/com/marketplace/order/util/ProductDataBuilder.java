package com.marketplace.order.util;

import com.marketplace.product.web.model.Product;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDataBuilder {

    public static Product.ProductBuilder buildProductWithAllFields() {
        return Product.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .ownerId(String.valueOf(UUID.randomUUID()))
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99));
    }

}
