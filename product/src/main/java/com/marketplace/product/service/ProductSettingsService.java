package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.List;
import java.util.Set;

public interface ProductSettingsService {

    List<Product> findAllByIdIn(Set<String> productIds);

    boolean containsInactiveProduct(List<Product> products);

}
