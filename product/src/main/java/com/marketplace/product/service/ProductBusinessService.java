package com.marketplace.product.service;

import com.marketplace.product.exception.ProductNotAvailableException;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBusinessService implements ProductManagerService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAllByIdIn(Set<String> productIds) {
        return productRepository.findAllByIdIn(productIds);
    }

    @Override
    public void decreaseProductsAmountAndSave(List<Product> products) {
        products.forEach(product -> {
            boolean decreasedAmount = product.decreaseAmount();

            if (!decreasedAmount) {
                throw new ProductNotAvailableException("Product " + product.getName() + " is out of stock");
            } else if (product.getAmount() == 0) {
                product.setActive(false);
            }
        });

        productRepository.saveAll(products);
    }
}
