package com.marketplace.product.service;

import com.marketplace.product.web.model.Product;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceImplTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    void shouldReturnAllProducts() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }

    @Test
    void shouldReturnProductById() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        String id = product.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(id);

        assertEquals(product, result);
    }

    @Test
    void shouldThrowExceptionIfProductNotFound() {
        String id = UUID.randomUUID().toString();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(id);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void shouldCreateProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        when(productRepository.save(product)).thenReturn(product);

        Product created = productService.createProduct(product);

        assertEquals(product, created);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldUpdateProduct() {
        Product original = ProductDataBuilder.buildProductWithAllFields().build();
        String id = original.getId();
        Product updated = ProductDataBuilder.buildProductWithAllFields()
                .id(id)
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(original));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        Product result = productService.updateProduct(id, updated);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(199.99), result.getPrice());
    }

    @Test
    void shouldDeleteProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        String id = product.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.deleteProduct(id);

        verify(productRepository, times(1)).delete(product);
    }
}
