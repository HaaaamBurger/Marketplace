package com.marketplace.product.service;

import com.marketplace.auth.web.model.User;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.product.util.UserDataBuilder;
import com.marketplace.product.web.dto.ProductCreateRequest;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Exception exception = assertThrows(EntityNotFoundException.class, () -> productService.findById(id));
        assertThat(exception.getMessage()).isEqualTo("Product not found with id: " + id);
    }

    @Test
    void shouldCreateProduct() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        SecurityContextHolder.setContext(mockSecurityContext);

        when(productRepository.save(product)).thenReturn(product);

        Product resultProduct = productService.create(productCreateRequest);

        assertEquals(product, resultProduct);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldUpdateProduct() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(user.getId())
                .build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .id(product.getId())
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .build();

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        SecurityContextHolder.setContext(mockSecurityContext);

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
        User user = UserDataBuilder.buildUserWithAllFields().build();
        product.setOwnerId(user.getId());

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        SecurityContextHolder.setContext(mockSecurityContext);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        productService.delete(product.getId());
        verify(productRepository, times(1)).delete(product);
    }
}
