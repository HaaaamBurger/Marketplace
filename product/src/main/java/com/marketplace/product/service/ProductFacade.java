package com.marketplace.product.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.mapper.ProductEntityMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ProductFacade implements ProductCrudService {

    private final ProductRepository productRepository;

    // TODO think about DI about all mappers
    private final ProductEntityMapper productEntityMapper;

    private final AuthenticationUserService authenticationUserService;

    private final UserSettingsService userSettingsService;

    @Override
    public Product create(ProductRequest productRequest) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        Product product = productEntityMapper.mapProductRequestDtoToProduct(productRequest).toBuilder()
                .ownerId(authenticatedUser.getId())
                .build();

        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT_SERVICE_FACADE]: Product not found by ID {}", productId);
                    return new EntityNotFoundException("Product not found!");
                });
    }

    @Override
    public Product update(String productId, ProductRequest productRequest) {

        Product product = validateProductAccessOrThrow(productId);

        Optional.ofNullable(productRequest.getName()).ifPresent(product::setName);
        Optional.ofNullable(productRequest.getPrice()).ifPresent(product::setPrice);
        Optional.of(productRequest.getAmount()).ifPresent(product::setAmount);
        Optional.ofNullable(productRequest.getDescription()).ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = validateProductAccessOrThrow(productId);
        productRepository.delete(product);
    }

    private Product validateProductAccessOrThrow(String productId) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Product product = getById(productId);

        if (userSettingsService.validateEntityOwnerOrAdmin(authenticatedUser, product.getOwnerId())) {
            return product;
        }

        log.error("[PRODUCT_SERVICE_FACADE]: User {} is not owner of the product: {} or not ADMIN", authenticatedUser.getId(), productId);
        throw new AccessDeniedException("Access denied!");
    }

}
