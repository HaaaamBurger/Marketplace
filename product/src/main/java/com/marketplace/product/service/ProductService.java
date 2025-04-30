package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;

import java.util.List;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(String id);

    Product createProduct(Product product);

    Product updateProduct(String id, Product updatedProduct);

    void deleteProduct(String id);

}