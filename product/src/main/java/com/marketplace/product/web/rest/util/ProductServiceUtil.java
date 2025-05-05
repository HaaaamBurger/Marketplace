package com.marketplace.product.web.rest.util;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceUtil {

    private final AuthHelper authHelper;

    private final ProductRepository productRepository;

    public Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT_SERVICE_IMPL]: Product not found by ID {}", productId);
                    return new EntityNotFoundException("Product not found!");
                });
    }

    public Product validateProductAccessOrThrow(String productId) {
        User authenticatedUser = authHelper.getAuthenticatedUser();
        Product product = findProductOrThrow(productId);

        if (checkUserOwnerOrAdmin(authenticatedUser, product.getUserId())) {
            return product;
        }

        log.error("[PRODUCT_SERVICE_IMPL]: User {} is not owner of the product: {} or not ADMIN", authenticatedUser.getId(), productId);
        throw new AccessDeniedException("Access denied!");
    }

    public boolean checkUserOwnerOrAdmin(User user, String productUserId) {
        return Objects.equals(user.getId(), productUserId) || user.getRole() == UserRole.ADMIN;
    }

}
