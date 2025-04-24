package com.marketplace.product.service.impl;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

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
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

}
