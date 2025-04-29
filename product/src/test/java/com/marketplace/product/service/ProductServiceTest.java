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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    void shouldReturnAllProducts() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.findAll();

        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    void shouldReturnProductById() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Product result = productService.findById(product.getId());
        assertEquals(product, result);
    }

    @Test
    void shouldThrowExceptionIfProductNotFound() {
        String id = UUID.randomUUID().toString();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.findById(id);
        });

        assertThat(exception.getMessage()).isEqualTo("Product not found with id: " + id);
    }

    @Test
    void shouldCreateProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        when(productRepository.save(product)).thenReturn(product);

        Product resultProduct = productService.create(product);

        assertEquals(product, resultProduct);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldUpdateProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .id(product.getId())
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product resultProduct = productService.update(product.getId(), updatedProduct);

        assertEquals("Updated Name", resultProduct.getName());
        assertEquals("Updated Description", resultProduct.getDescription());
        assertEquals(BigDecimal.valueOf(199.99), resultProduct.getPrice());
    }

    @Test
    void shouldDeleteProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        productService.delete(product.getId());
        verify(productRepository, times(1)).delete(product);
    }
}
