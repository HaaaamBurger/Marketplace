package com.marketplace.product.service;

import com.marketplace.product.exception.ProductNotFoundException;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return findProductByIdOrThrowException(id);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product updatedProduct) {
        Product existingProduct = findProductByIdOrThrowException(id);

        Optional.ofNullable(updatedProduct.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existingProduct::setDescription);

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(String id) {
        Product product = findProductByIdOrThrowException(id);
        productRepository.delete(product);
    }

    private Product findProductByIdOrThrowException(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

}
