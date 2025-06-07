package com.marketplace.product.mapper;

import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;

import java.util.List;

public interface SimpleProductMapper {

    ProductResponse mapProductToProductResponseDto(Product product);

    Product mapProductRequestDtoToProduct(ProductRequest productRequest);

    ProductRequest mapProductToProductRequestDto(Product product);

    List<ProductResponse> mapProductsToProductResponseDtos(List<Product> products);

}
