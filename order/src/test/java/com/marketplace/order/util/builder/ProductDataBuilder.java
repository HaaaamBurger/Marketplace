package com.marketplace.order.util.builder;

import com.marketplace.product.web.model.Product;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDataBuilder {

    public static Product.ProductBuilder buildProductWithAllFields() {
        return Product.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .ownerId(String.valueOf(UUID.randomUUID()))
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .photoUrl(String.valueOf(UUID.randomUUID()))
                .amount(1)
                .active(true)
                .description("Test Description");
    }

}
