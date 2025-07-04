package com.marketplace.order.service;

import com.marketplace.aws.service.S3FileBusinessService;
import com.marketplace.aws.service.S3ProductPhotoService;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.builder.OrderDataBuilder;
import com.marketplace.order.util.builder.ProductDataBuilder;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.MongoProductCrudService;
import com.marketplace.product.web.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
public class ProductEventServiceTest {

    @MockitoBean
    private MongoProductCrudService mongoProductCrudService;

    @MockitoSpyBean
    private OrderBusinessService orderBusinessService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private S3FileBusinessService s3FileBusinessService;

    @MockitoBean
    private S3ProductPhotoService s3ProductPhotoService;

    @MockitoBean
    private OrderRepository orderRepository;

    @Autowired
    private ProductEventService productEventService;

    @Test
    public void deleteProductInstances_ShouldDeleteAllInstances() {
        String fileName = "fileName.png";
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .products(new HashSet<>(Set.of(product)))
                .build();

        when(mongoProductCrudService.getById(product.getId())).thenReturn(product);
        when(orderRepository.findByProductsIdsAndStatuses(Set.of(product.getId()), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS))).thenReturn(new ArrayList<>(List.of(order)));
        when(s3FileBusinessService.getFilenameFromUrl(product.getPhotoUrl())).thenReturn(fileName);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productEventService.deleteProductInstances(product.getId());

        verify(mongoProductCrudService).getById(product.getId());
        verify(orderRepository).findByProductsIdsAndStatuses(Set.of(product.getId()), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));
        verify(orderRepository).delete(order);
        verify(s3FileBusinessService).getFilenameFromUrl(product.getPhotoUrl());
        verify(productRepository).findById(product.getId());
        verify(productRepository).delete(product);
    }

    @Test
    public void deleteProductInstances_ShouldDoNothing_WhenProductNotFound() {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(mongoProductCrudService.getById(product.getId())).thenThrow(EntityNotFoundException.class);

        productEventService.deleteProductInstances(product.getId());

        verify(mongoProductCrudService).getById(product.getId());
        verify(orderRepository, never()).findByProductsIdsAndStatuses(Set.of(product.getId()), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));
        verify(orderRepository, never()).delete(any());
        verify(s3FileBusinessService, never()).getFilenameFromUrl(product.getPhotoUrl());
        verify(productRepository, never()).findById(product.getId());
        verify(productRepository, never()).delete(product);
    }
}
