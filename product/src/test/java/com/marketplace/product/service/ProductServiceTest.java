package com.marketplace.product.service;

import com.marketplace.product.model.Product;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.web.rest.ProductRepository;
import com.marketplace.product.web.rest.ProductService;
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

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleProduct = ProductDataBuilder.buildProductWithAllFields().build();
    }

    @Test
    void shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }

    @Test
    void shouldReturnProductById() {
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById(id);

        assertEquals(sampleProduct, result);
    }

    @Test
    void shouldThrowExceptionIfProductNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(id);
        });

        assertTrue(exception.getMessage().contains("not found"));
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
        Product updated = ProductDataBuilder.buildProductWithAllFields()
                .id(id)
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        Product result = productService.updateProduct(id, updated);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(199.99), result.getPrice());
    }

    @Test
    void shouldDeleteProduct() {
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        productService.deleteProduct(id);

        verify(productRepository, times(1)).delete(sampleProduct);
    }
}
