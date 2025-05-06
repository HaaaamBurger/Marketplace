package com.marketplace.product.service.impl;

import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.rest.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.rest.util.ProductServiceUtil;
import com.marketplace.product.web.util.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductEntityMapper productEntityMapper;

    private final ProductServiceUtil productServiceUtil;

    private final AuthHelper authHelper;

    @Override
    public Product create(ProductRequest productRequest) {
        User authenticatedUser = authHelper.getAuthenticatedUser();

        Product product = productEntityMapper.mapRequestDtoToEntity(productRequest).toBuilder()
                .ownerId(authenticatedUser.getId())
                .build();

        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String productId) {
        return productServiceUtil.findProductOrThrow(productId);
    }

    @Override
    public Product update(String productId, ProductRequest productRequest) {

        Product product = productServiceUtil.validateProductAccessOrThrow(productId);

        Optional.ofNullable(productRequest.getName()).ifPresent(product::setName);
        Optional.ofNullable(productRequest.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(productRequest.getDescription()).ifPresent(product::setDescription);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        Product product = productServiceUtil.validateProductAccessOrThrow(productId);
        productRepository.delete(product);
    }

}
