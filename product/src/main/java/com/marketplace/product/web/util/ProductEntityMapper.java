package com.marketplace.product.web.util;

import com.marketplace.common.util.EntityMapper;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductEntityMapper implements EntityMapper<Product, ProductRequest,ProductResponse> {

    @Override
    public ProductResponse mapEntityToResponseDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public Product mapRequestDtoToEntity(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .build();
    }

    public List<ProductResponse> mapEntitiesToResponseDtos(List<Product> products) {
        return products.stream()
                .map(this::mapEntityToResponseDto)
                .toList();
    }

}
