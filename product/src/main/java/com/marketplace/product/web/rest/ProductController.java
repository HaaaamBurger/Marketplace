package com.marketplace.product.web.rest;

import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.rest.dto.ProductResponse;
import com.marketplace.product.web.rest.dto.ProductRequest;
import com.marketplace.product.web.model.Product;

import com.marketplace.product.web.util.ProductEntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    private final ProductEntityMapper productEntityMapper;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(productEntityMapper.mapEntitiesToResponseDtos(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        Product product = productService.findById(productId);
        return ResponseEntity.ok(productEntityMapper.mapEntityToResponseDto(product));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.create(productRequest);
        return ResponseEntity.ok(productEntityMapper.mapEntityToResponseDto(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
             @PathVariable String productId,
             @Valid @RequestBody ProductRequest productRequest
    ) {
        Product product = productService.update(productId, productRequest);
        return ResponseEntity.ok(productEntityMapper.mapEntityToResponseDto(product));
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        productService.delete(productId);
    }
}
