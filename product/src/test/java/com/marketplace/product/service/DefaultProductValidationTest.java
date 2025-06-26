package com.marketplace.product.service;

import com.marketplace.product.config.ProductApplicationConfig;
import com.marketplace.product.exception.ProductNotAvailableException;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.web.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = ProductApplicationConfig.class)
public class DefaultProductValidationTest {

    @Autowired
    private DefaultProductValidationService defaultProductValidationService;

    @Test
    public void validateProductOrThrow_ShouldValidateProductSuccessfully() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        defaultProductValidationService.validateProductOrThrow(product);
    }

    @Test
    public void validateProductOrThrow_ShouldThrowException_WhenAmountIsNotEnough() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(0)
                .build();

        assertThatThrownBy(() -> defaultProductValidationService.validateProductOrThrow(product)).isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    public void validateProductOrThrow_ShouldThrowException_WhenNotActive() {
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .active(false)
                .build();

        assertThatThrownBy(() -> defaultProductValidationService.validateProductOrThrow(product)).isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    public void validateProducts_ShouldReturnProductsValidationResponseWithValidData() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        boolean validateProducts = defaultProductValidationService.validateProducts(List.of(product, product1));

        assertThat(validateProducts).isFalse();
    }

    @Test
    public void validateProducts_ShouldReturnProductsValidationResponseWithInValidData() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Product product2 = ProductDataBuilder.buildProductWithAllFields()
                .active(false)
                .build();
        Product product3 = ProductDataBuilder.buildProductWithAllFields()
                .amount(0)
                .build();

        boolean validateProducts = defaultProductValidationService.validateProducts(List.of(product, product1, product2, product3));

        assertThat(validateProducts).isTrue();
    }

}
