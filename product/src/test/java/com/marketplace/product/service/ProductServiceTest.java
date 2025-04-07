package com.marketplace.product.service;

import com.marketplace.product.model.Product;
import com.marketplace.product.web.rest.ProductRepository;
import com.marketplace.product.web.rest.ProductService;
import com.marketplace.product.exception.EntityFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntityFetcher entityFetcher;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleProduct = new Product(
                UUID.randomUUID(),
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99)
        );
    }

    @Test
    void shouldReturnAllProducts() {
        List<Product> products = List.of(sampleProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }

    @Test
    void shouldReturnProductById() {
        UUID id = sampleProduct.getId();
        when(entityFetcher.fetchById(productRepository, id, "Product")).thenReturn(sampleProduct);

        Product result = productService.getProductById(id);

        assertEquals(sampleProduct, result);
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product created = productService.createProduct(sampleProduct);

        assertEquals(sampleProduct, created);
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void shouldUpdateProduct() {
        UUID id = sampleProduct.getId();
        Product updated = new Product(id, "Updated Name", "Updated Description", BigDecimal.valueOf(199.99));
        when(entityFetcher.fetchById(productRepository, id, "Product")).thenReturn(sampleProduct);
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        Product result = productService.updateProduct(id, updated);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(199.99), result.getPrice());
    }

    @Test
    void shouldDeleteProduct() {
        UUID id = sampleProduct.getId();
        when(entityFetcher.fetchById(productRepository, id, "Product")).thenReturn(sampleProduct);

        productService.deleteProduct(id);

        verify(productRepository, times(1)).delete(sampleProduct);
    }
}
