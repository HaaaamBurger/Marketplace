package com.marketplace.product.web.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.common.util.EntityMapper;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductEntityMapper implements EntityMapper<Product, ProductResponse> {

    @Override
    public ProductResponse mapEntityToDto(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Product mapDtoToEntity(ProductResponse dto) {
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> mapEntitiesToDtos(List<Product> products) {
        return products.stream()
                .map(this::mapEntityToDto)
                .toList();
    }

}
