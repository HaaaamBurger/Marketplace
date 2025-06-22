package com.marketplace.main.util.builder;

import com.marketplace.product.web.dto.ProductRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductRequestDataBuilder {

    public static ProductRequest.ProductRequestBuilder buildProductWithAllFields() {
        return ProductRequest.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .active(true)
                .photo(new MockMultipartFile("photo", "photo.png", "image/png", "photo".getBytes()))
                .amount(1)
                .price(BigDecimal.ONE);
    }

}
