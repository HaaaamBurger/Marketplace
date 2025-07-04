package com.marketplace.order.service;

import com.marketplace.aws.service.S3FileManagerService;
import com.marketplace.aws.service.S3FileUploadService;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventService {

    private final OrderManagerService orderManagerService;

    private final ProductRepository productRepository;

    private final ProductCrudService productCrudService;

    private final S3FileManagerService s3FileManagerService;

    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public void deleteProductInstances(String productId) {
        try {
            Product product = productCrudService.getById(productId);
            orderManagerService.removeProductFromAllOrders(product);

            String filename = s3FileManagerService.getFilenameFromUrl(product.getPhotoUrl());
            s3FileUploadService.deleteFile(filename);
            deleteProductIfExists(productId);
        } catch (EntityNotFoundException e) {
            log.error("[PRODUCT_EVENT_SERVICE]: Product by id {} not found", productId);
        }
    }

    private void deleteProductIfExists(String productId) {
        productRepository.findById(productId).ifPresent(productRepository::delete);
    }

}
