package com.marketplace.product.web.rest;

import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.dto.ProductCreateRequest;
import com.marketplace.product.web.dto.ProductResponse;
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
        return ResponseEntity.ok(productEntityMapper.mapEntitiesToDtos(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        Product product = productService.findById(productId);
        return ResponseEntity.ok(productEntityMapper.mapEntityToDto(product));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest productCreateRequest) {
        Product product = productService.create(productCreateRequest);
        return ResponseEntity.ok(productEntityMapper.mapEntityToDto(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
             @PathVariable String productId,
             @Valid @RequestBody Product updatedProduct
    ) {
        return ResponseEntity.ok(productService.update(productId, updatedProduct));
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        productService.delete(productId);
    }
}
