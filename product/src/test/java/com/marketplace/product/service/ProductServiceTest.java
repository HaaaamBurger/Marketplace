package com.marketplace.product.service;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.web.model.User;
import com.marketplace.product.util.MockHelper;
import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class ProductServiceTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private MockHelper mockHelper;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void findById_shouldReturnProductById() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        Product result = productService.findById(product.getId());
        assertEquals(product, result);
    }

    @Test
    public void findById_shouldThrowExceptionIfProductNotFound() {
        String id = UUID.randomUUID().toString();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> productService.findById(id));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");
    }

    @Test
    public void create_shouldCreateProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        ProductRequest productRequest = ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Product responseProduct = productService.create(productRequest);

        assertThat(product.getName()).isEqualTo(responseProduct.getName());
        assertThat(product.getDescription()).isEqualTo(responseProduct.getDescription());
        assertThat(product.getPrice()).isEqualTo(responseProduct.getPrice());
    }

    @Test
    public void create_shouldThrowException_WhenNoSecurity() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        ProductRequest productRequest = ProductRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> productService.create(productRequest));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void findAll_shouldReturnAllProducts() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.findAll();

        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    public void update_shouldUpdateProduct() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .userId(user.getId())
                .build();
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product resultProduct = productService.update(product.getId(), productRequest);

        assertThat(productRequest.getName()).isEqualTo(resultProduct.getName());
        assertThat(productRequest.getDescription()).isEqualTo(resultProduct.getDescription());
        assertThat(productRequest.getPrice()).isEqualTo(resultProduct.getPrice());
    }

    @Test
    public void delete_shouldDeleteProduct() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        User user = mockHelper.mockAuthenticationAndSetContext();
        product.setUserId(user.getId());

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        productService.delete(product.getId());

        verify(productRepository, times(1)).delete(product);
    }
}
