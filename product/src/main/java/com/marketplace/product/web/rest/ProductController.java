package com.marketplace.product.web.rest;

import com.marketplace.product.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productid}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID productid) {
        Product product = productService.getProductById(productid);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{productid}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID productid, @RequestBody Product updatedProduct) {
        return ResponseEntity.ok(productService.updateProduct(productid, updatedProduct));
    }

    @DeleteMapping("/{productid}")
    public void deleteProduct(@PathVariable UUID productid) {
        productService.deleteProduct(productid);
    }
}
