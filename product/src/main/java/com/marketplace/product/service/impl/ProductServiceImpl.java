package com.marketplace.product.service.impl;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.dto.ProductCreateRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    public Product create(ProductCreateRequest productCreateRequest) {
        User principalUserId = getPrincipalUser();

        Product product = Product.builder()
                .name(productCreateRequest.getName())
                .ownerId(principalUserId.getId())
                .description(productCreateRequest.getDescription())
                .price(productCreateRequest.getPrice())
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product update(String productId, Product updatedProduct) {

        Product product = validateProductOwnerElseThrowException(productId);

        Optional.ofNullable(updatedProduct.getName()).ifPresent(product::setName);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = validateProductOwnerElseThrowException(productId);
        productRepository.delete(product);
    }

    private Product findProductByIdOrThrowException(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }

    private User getPrincipalUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private Product validateProductOwnerElseThrowException(String productId) {
        User principalUser = getPrincipalUser();
        Product product = findProductByIdOrThrowException(productId);

        if (checkUserOwnerOrAdmin(principalUser, product.getOwnerId())) {
            return product;
        }

        throw new AccessDeniedException("User: " + principalUser.getId() + " is not owner of product: " + productId);
    }

    private boolean checkUserOwnerOrAdmin(User user, String productOwnerId) {
        return Objects.equals(user.getId(), productOwnerId) || user.getRole() == UserRole.ADMIN;
    }
}
