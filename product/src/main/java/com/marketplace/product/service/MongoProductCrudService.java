package com.marketplace.product.service;

import com.marketplace.aws.service.S3FileUploadService;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.kafka.producer.ProductEventProducer;
import com.marketplace.product.mapper.SimpleProductMapper;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.DefaultUserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoProductCrudService implements ProductCrudService {

    private final ProductRepository productRepository;

    private final SimpleProductMapper simpleProductMapper;

    private final AuthenticationUserService authenticationUserService;

    private final S3FileUploadService s3FileUploadService;

    private final DefaultUserValidationService defaultUserValidationService;

    private final ProductEventProducer productEventProducer;

    @Transactional
    @Override
    public Product create(ProductRequest productRequest) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        Product product = simpleProductMapper.mapProductRequestDtoToProduct(productRequest).toBuilder()
                .ownerId(authenticatedUser.getId())
                .build();

        if (productRequest.getPhoto() != null) {
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
                .orElseThrow(() -> new EntityNotFoundException("Product not found!"));
    }

    @Transactional
    @Override
    public Product update(String productId, ProductRequest productRequest) {
        Product product = validateProductAccessOrThrow(productId);

        applyUpdatesByRequest(product, productRequest);

        return productRepository.save(product);
    }

    @Override
    public void delete(String productId) {
        validateProductAccessOrThrow(productId);
        productEventProducer.sendDeleteProductFromOrdersEvent(productId);
    }

    private Product validateProductAccessOrThrow(String productId) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Product product = getById(productId);

        if (defaultUserValidationService.validateEntityOwnerOrAdmin(authenticatedUser, product.getOwnerId())) {
            return product;
        }

        throw new AccessDeniedException("Access denied!");
    }

    private void applyUpdatesByRequest( Product product, ProductRequest productRequest) {
        Optional.ofNullable(productRequest.getName()).ifPresent(product::setName);
        Optional.ofNullable(productRequest.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(productRequest.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(productRequest.getActive()).ifPresent(product::setActive);

        Optional.ofNullable(productRequest.getAmount()).ifPresent(amount -> {
            if (amount == 0) {
                product.setActive(false);
            }
            product.setAmount(amount);
        });

        // TODO delete old photo
        Optional.ofNullable(productRequest.getPhoto()).ifPresent(multipartFile -> {
            URL url = s3FileUploadService.uploadFile(productRequest.getPhoto(), String.valueOf(UUID.randomUUID()));
            product.setPhotoUrl(String.valueOf(url));
        });
    }

}
