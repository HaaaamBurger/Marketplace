package com.marketplace.product.web.rest;

import com.marketplace.product.model.Product;
import com.marketplace.product.exception.EntityFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final EntityFetcher entityFetcher;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(UUID id) {
        return entityFetcher.fetchById(productRepository, id, "Product");
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, Product updatedProduct) {
        Product existingProduct = entityFetcher.fetchById(productRepository, id, "Product");

        Optional.ofNullable(updatedProduct.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existingProduct::setDescription);

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(UUID id) {
        Product product = entityFetcher.fetchById(productRepository, id, "Product");
        productRepository.delete(product);
    }
}
