package com.marketplace.product.web.rest;

import com.marketplace.product.model.Product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{productid}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID productid) {
        Product product = productService.getProductById(productid);
        return ResponseEntity.ok(product);
    }


    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{productid}")
    public ResponseEntity<Product> updateProduct(@Valid @PathVariable UUID productid, @RequestBody Product updatedProduct) {
        return ResponseEntity.ok(productService.updateProduct(productid, updatedProduct));
    }

    @DeleteMapping("/{productid}")
    public void deleteProduct(@PathVariable UUID productid) {
        productService.deleteProduct(productid);
    }
}
