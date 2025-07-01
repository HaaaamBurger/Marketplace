package com.marketplace.product.service;

import com.marketplace.product.config.ProductApplicationConfig;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.web.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = ProductApplicationConfig.class)
public class ProductBusinessServiceTest {

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private DefaultProductValidationService defaultProductValidationService;

    @Autowired
    private ProductBusinessService productBusinessService;

    @Test
    public void findAllByIdIn_ShouldReturnProductsByIdIn() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findAllByIdIn(Set.of(product.getId(), product1.getId()))).thenReturn(List.of(product, product1));

        List<Product> products = productBusinessService.findAllByIdIn(Set.of(product.getId(), product1.getId()));

        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
        assertThat(products.get(0).getId()).isEqualTo(product.getId());
        assertThat(products.get(1).getId()).isEqualTo(product1.getId());

        verify(productRepository).findAllByIdIn(Set.of(product.getId(), product1.getId()));
    }

    @Test
    public void decreaseProductsAmountAndSave_ShouldDecreaseProductsAmountAndSave() {
        Product mockedProduct = mock(Product.class);

        when(mockedProduct.decreaseAmount()).thenReturn(true);
        when(productRepository.saveAll(List.of(mockedProduct))).thenAnswer(invocation -> invocation.getArgument(0));

        productBusinessService.decreaseProductsAmountAndSave(Set.of(mockedProduct));

        verify(mockedProduct).decreaseAmount();
        verify(productRepository).saveAll(List.of(mockedProduct));
    }

    @Test
    public void decreaseProductsAmountAndSave_ShouldThrowException_WhenProductAmountIsNotEnough() {
        Product mockedProduct = mock(Product.class);

        when(mockedProduct.decreaseAmount()).thenReturn(true);
        when(mockedProduct.getAmount()).thenReturn(0);
        when(productRepository.saveAll(List.of(mockedProduct))).thenAnswer(invocation -> invocation.getArgument(0));

        productBusinessService.decreaseProductsAmountAndSave(Set.of(mockedProduct));

        assertThat(mockedProduct.getActive()).isFalse();

        verify(mockedProduct).decreaseAmount();
        verify(mockedProduct).getAmount();
        verify(productRepository).saveAll(List.of(mockedProduct));
    }

}
