package com.marketplace.product.service.impl;

import com.marketplace.auth.exception.EntityNotFoundException;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    public Product create(ProductRequest productRequest) {
        User principalUserId = getPrincipalUser();

        Product product = productEntityMapper.mapRequestDtoToEntity(productRequest).toBuilder()
                .ownerId(principalUserId.getId())
                .build();

        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String productId) {
        return findProductByIdOrThrowException(productId);
    }

    @Override
    public Product update(String productId, ProductRequest productRequest) {

        Product product = validateProductOwnerElseThrowException(productId);

        Optional.ofNullable(productRequest.getName()).ifPresent(product::setName);
        Optional.ofNullable(productRequest.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(productRequest.getDescription()).ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = validateProductOwnerElseThrowException(productId);
        productRepository.delete(product);
    }

    private User getPrincipalUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.error("[PRODUCT_SERVICE_IMPL]: Authentication is null");
            throw new AuthenticationCredentialsNotFoundException("Authentication is unavailable!");
        }

        return (User) authentication.getPrincipal();
    }

    private Product findProductByIdOrThrowException(String productId) {
        log.error("[PRODUCT_SERVICE_IMPL]: Product not found by id {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found!"));
    }

    private Product validateProductOwnerElseThrowException(String productId) {
        User principalUser = getPrincipalUser();
        Product product = findProductByIdOrThrowException(productId);

        if (checkUserOwnerOrAdmin(principalUser, product.getOwnerId())) {
            return product;
        }

        log.error("[PRODUCT_SERVICE_IMPL]: User {} is not owner of product: {} or not ADMIN", principalUser.getId(), productId);
        throw new AccessDeniedException("Access denied!");
    }

    private boolean checkUserOwnerOrAdmin(User user, String productOwnerId) {
        return Objects.equals(user.getId(), productOwnerId) || user.getRole() == UserRole.ADMIN;
    }
}
