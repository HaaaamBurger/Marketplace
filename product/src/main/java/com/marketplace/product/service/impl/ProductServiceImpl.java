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
public final class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String productId) {
        return findProductByIdOrThrowException(productId);
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(String productId, Product updatedProduct) {
        Product existingProduct = findProductByIdOrThrowException(productId);

        Optional.ofNullable(updatedProduct.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existingProduct::setDescription);

        return productRepository.save(existingProduct);
    }

    @Override
    public void delete(String productId) {
        Product product = findProductByIdOrThrowException(productId);
        productRepository.delete(product);
    }

    private Product findProductByIdOrThrowException(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }

}
