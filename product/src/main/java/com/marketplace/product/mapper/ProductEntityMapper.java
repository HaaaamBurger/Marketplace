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
                .description(product.getDescription())
                .price(product.getPrice())
                .amount(product.getAmount())
                .photoUrl(product.getPhotoUrl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product mapProductRequestDtoToProduct(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .amount(productRequest.getAmount())
                .active(productRequest.getActive())
                .build();
    }

    public ProductRequest mapProductToProductRequestDto(Product product) {
        return ProductRequest.builder()
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .amount(product.getAmount())
                .active(product.getActive())
                .build();
    }

    public List<ProductResponse> mapProductsToProductResponseDtos(List<Product> products) {
        return products.stream()
                .map(this::mapProductToProductResponseDto)
                .toList();
    }

}
