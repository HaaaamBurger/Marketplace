package com.marketplace.product.service.impl;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.util.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductEntityMapper productEntityMapper;

    private final AuthHelper authHelper;

    @Override
    public Product create(ProductRequest productRequest) {
        User authenticatedUser = authHelper.getAuthenticatedUser();

        Product product = productEntityMapper.mapRequestDtoToEntity(productRequest).toBuilder()
                .userId(authenticatedUser.getId())
                .build();

        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String productId) {
        return findProductOrThrow(productId);
    }

    @Override
    public Product update(String productId, ProductRequest productRequest) {

        Product product = validateProductAccessOrThrow(productId);

        Optional.ofNullable(productRequest.getName()).ifPresent(product::setName);
        Optional.ofNullable(productRequest.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(productRequest.getDescription()).ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = validateProductAccessOrThrow(productId);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT_SERVICE_IMPL]: Product not found by ID {}", productId);
                    return new EntityNotFoundException("Product not found!");
                });
    }

    private Product validateProductAccessOrThrow(String productId) {
        User authenticatedUser = authHelper.getAuthenticatedUser();
        Product product = findProductOrThrow(productId);

        if (checkUserOwnerOrAdmin(authenticatedUser, product.getUserId())) {
            return product;
        }

        log.error("[PRODUCT_SERVICE_IMPL]: User {} is not owner of the product: {} or not ADMIN", authenticatedUser.getId(), productId);
        throw new AccessDeniedException("Access denied!");
    }

    private boolean checkUserOwnerOrAdmin(User user, String productUserId) {
        return Objects.equals(user.getId(), productUserId) || user.getRole() == UserRole.ADMIN;
    }

}
