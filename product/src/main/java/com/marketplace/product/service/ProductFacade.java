package com.marketplace.product.service;

import com.marketplace.aws.service.S3FileUploadService;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.mapper.SimpleProductMapper;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacade implements ProductCrudService, ProductSettingsService {

    private final ProductRepository productRepository;

    private final SimpleProductMapper simpleProductMapper;

    private final AuthenticationUserService authenticationUserService;

    private final UserSettingsService userSettingsService;

    private final S3FileUploadService s3FileUploadService;

    @Transactional
    @Override
    public Product create(ProductRequest productRequest) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        Product product = simpleProductMapper.mapProductRequestDtoToProduct(productRequest).toBuilder()
                .ownerId(authenticatedUser.getId())
                .build();

        if (productRequest.getPhoto() != null && !productRequest.getPhoto().isEmpty()) {
            URL url = s3FileUploadService.uploadFile(productRequest.getPhoto(), String.valueOf(UUID.randomUUID()));
            product.setPhotoUrl(url.toString());
        }

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
        Optional.ofNullable(productRequest.getActive()).ifPresent(product::setActive);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = validateProductAccessOrThrow(productId);
        productRepository.delete(product);
    }

    @Override
    public List<Product> findAllByIdIn(Set<String> productIds) {
        return productRepository.findAllByIdIn(productIds);
    }

    @Override
    public boolean containsInactiveProduct(List<Product> products) {
        return products.stream().anyMatch(product -> !product.getActive());
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
