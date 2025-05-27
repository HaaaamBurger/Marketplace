package com.marketplace.product.mapper;

import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductEntityMapper {

    public ProductResponse mapProductToProductResponseDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .ownerId(product.getOwnerId())
                .price(product.getPrice())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product mapProductRequestDtoToProduct(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .build();
    }

    public ProductRequest mapProductToProductRequestDto(Product product) {
        return ProductRequest.builder()
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }

    public List<ProductResponse> mapProductsToProductResponseDtos(List<Product> products) {
        return products.stream()
                .map(this::mapProductToProductResponseDto)
                .toList();
    }

}
