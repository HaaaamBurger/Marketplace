package com.marketplace.order.service;

import com.marketplace.aws.service.S3FileManagerService;
import com.marketplace.aws.service.S3FileUploadService;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductEventService {

    private final OrderManagerService orderManagerService;

    private final ProductRepository productRepository;

    private final ProductCrudService productCrudService;

    private final S3FileManagerService s3FileManagerService;

    private final S3FileUploadService s3FileUploadService;

    // TODO update tests
    @Transactional
    public void deleteProductFromOrdersAndProduct(String productId) {
        Product product = productCrudService.getById(productId);
        orderManagerService.removeProductFromAllOrders(product);

        String filename = s3FileManagerService.getFilenameFromUrl(product.getPhotoUrl());
        s3FileUploadService.deleteFile(filename);

        deleteProductIfExists(productId);
    }

    private void deleteProductIfExists(String productId) {
        productRepository.findById(productId).ifPresent(productRepository::delete);
    }

}
