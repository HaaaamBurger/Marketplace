package com.marketplace.order.service;

import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductEventService {

    private final OrderManagerService orderManagerService;

    private final ProductRepository productRepository;

    @Transactional
    public void deleteProductFromOrdersAndProduct(String productId) {
        orderManagerService.removeProductFromAllOrders(productId);
        deleteProductIfExists(productId);
    }

    private void deleteProductIfExists(String productId) {
        productRepository.findById(productId).ifPresent(productRepository::delete);
    }

}
