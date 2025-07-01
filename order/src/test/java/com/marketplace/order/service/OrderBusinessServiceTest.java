package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.MockHelper;
import com.marketplace.order.util.builder.OrderDataBuilder;
import com.marketplace.order.util.builder.ProductDataBuilder;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.exception.ProductNotAvailableException;
import com.marketplace.product.service.DefaultProductValidationService;
import com.marketplace.product.service.MongoProductCrudService;
import com.marketplace.product.service.ProductBusinessService;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
public class OrderBusinessServiceTest {

    @MockitoBean
    private ProductBusinessService productBusinessService;

    @MockitoBean
    private DefaultProductValidationService defaultProductValidationService;

    @MockitoBean
    private MongoProductCrudService mongoProductCrudService;

    @MockitoBean
    private OrderRepository orderRepository;

    @Autowired
    private OrderBusinessService orderBusinessService;

    @Autowired
    private MockHelper mockHelper;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    public void addProductToOrder_ShouldAddProductToExistingOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(mongoProductCrudService.getById(product.getId())).thenReturn(product);
        when(orderRepository.findOrderByOwnerId(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderBusinessService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProducts()).isNotNull();
        assertThat(responseOrder.getProducts().size()).isEqualTo(1);
        assertThat(responseOrder.getProducts().stream().anyMatch(orderProduct -> orderProduct.getId().equals(product.getId()))).isTrue();
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(mongoProductCrudService, times(1)).getById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldCreateOrderAndAddProductToOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(mongoProductCrudService.getById(product.getId())).thenReturn(product);
        when(orderRepository.findOrderByOwnerId(user.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderBusinessService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProducts()).isNotNull();
        assertThat(responseOrder.getProducts().size()).isEqualTo(1);
        assertThat(responseOrder.getProducts().stream().anyMatch(productId -> productId.equals(product.getId()))).isTrue();
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(mongoProductCrudService, times(1)).getById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldThrowException_WhenProductNotExists() {
        mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(mongoProductCrudService.getById(product.getId())).thenThrow(new EntityNotFoundException("Product not found!"));

        assertThatThrownBy(() -> orderBusinessService.addProductToOrder(product.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found!");

        verify(mongoProductCrudService, times(1)).getById(product.getId());
    }

    @Test
    public void findOrderByOwnerIdAndStatus_ShouldReturnOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED)).thenReturn(Optional.of(order));

        Optional<Order> orderByOwnerIdAndStatus = orderBusinessService.findOrderByOwnerIdAndStatus(OrderStatus.CREATED);

        assertThat(orderByOwnerIdAndStatus).isPresent();
        assertThat(orderByOwnerIdAndStatus.get().getOwnerId()).isEqualTo(user.getId());

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED);
    }

    @Test
    public void findOrderByOwnerIdAndStatus_ShouldReturnEmptyOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED)).thenReturn(Optional.of(order));
        Optional<Order> orderByOwnerIdAndStatus = orderBusinessService.findOrderByOwnerIdAndStatus(OrderStatus.IN_PROGRESS);
        assertThat(orderByOwnerIdAndStatus).isNotPresent();

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
    }

    @Test
    public void findOrdersByOwnerIdAndStatusIn_ShouldReturnOrders() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        Order order1 = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderRepository.findOrdersByOwnerIdAndStatusIn(user.getId(), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS))).thenReturn(List.of(order, order1));
        List<Order> ordersByOwnerIdAndStatusIn = orderBusinessService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));

        assertThat(ordersByOwnerIdAndStatusIn).isNotNull();
        assertThat(ordersByOwnerIdAndStatusIn.size()).isEqualTo(2);
        assertThat(ordersByOwnerIdAndStatusIn.get(0).getStatus()).isEqualTo(order.getStatus());
        assertThat(ordersByOwnerIdAndStatusIn.get(1).getStatus()).isEqualTo(order1.getStatus());

        verify(orderRepository, times(1)).findOrdersByOwnerIdAndStatusIn(user.getId(), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));
    }

    @Test
    public void findOrdersByOwnerIdAndStatusIn_ShouldReturnEmptyOrders() {
        User user = mockHelper.mockAuthenticationAndSetContext();

        when(orderRepository.findOrdersByOwnerIdAndStatusIn(user.getId(), List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED))).thenReturn(List.of());
        List<Order> ordersByOwnerIdAndStatusIn = orderBusinessService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED));

        assertThat(ordersByOwnerIdAndStatusIn).isNotNull();
        assertThat(ordersByOwnerIdAndStatusIn.size()).isEqualTo(0);

        verify(orderRepository, times(1)).findOrdersByOwnerIdAndStatusIn(user.getId(), List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED));
    }

    @Test
    public void removeProductFromOrder_ShouldRemoveProductFromOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Product product2 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(new HashSet<>(Set.of(product1, product2)))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));

        orderBusinessService.removeProductFromOrder(product2.getId());

        assertThat(order.getProducts()).isNotNull();
        assertThat(order.getProducts().size()).isEqualTo(1);
        assertThat(order.getProducts().stream().anyMatch(product -> product.getId().equals(product1.getId()))).isTrue();

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void removeProductFromOrder_ShouldRemoveProductFromOrderAndRemoveOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(new HashSet<>(Set.of(product)))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS))
                .thenReturn(Optional.of(order));

        orderBusinessService.removeProductFromOrder(product.getId());

        verify(orderRepository).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository).deleteById(order.getId());
        verify(orderRepository, never()).save(order);
    }
    @Test
    public void removeProductFromOrder_ShouldNotRemoveProductFromOrder_WhenProductNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        String product1 = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(new HashSet<>(Set.of(product)))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));

        orderBusinessService.removeProductFromOrder(product1);

        assertThat(order.getProducts()).isNotNull();
        assertThat(order.getProducts().size()).isEqualTo(1);
        assertThat(order.getProducts().contains(product)).isTrue();

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).deleteById(order.getId());
        verify(orderRepository, never()).save(order);
    }

    @Test
    public void removeProductFromOrder_ShouldThrowException_WhenOrderNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderBusinessService.removeProductFromOrder(product.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
    }

    @Test
    public void payForOrder_ShouldPayForOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(Set.of(product))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        when(defaultProductValidationService.isNotValidProduct(product)).thenReturn(false);

        orderBusinessService.payForOrder();

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(productBusinessService, times(1)).decreaseProductsAmountAndSave(Set.of(product));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void payForOrder_ShouldThrowException_WhenOrderNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderBusinessService.payForOrder())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void payForOrder_ShouldThrowException_WhenProductAmountIsNotEnough() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(0)
                .build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(Set.of(product))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        doThrow(ProductNotAvailableException.class).when(defaultProductValidationService).validateProductOrThrow(product);

        assertThatThrownBy(() -> orderBusinessService.payForOrder())
                .isInstanceOf(ProductNotAvailableException.class);

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void payForOrder_ShouldThrowException_WhenProductIsNotActive() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .active(false)
                .build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .products(Set.of(product))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        doThrow(ProductNotAvailableException.class).when(defaultProductValidationService).validateProductOrThrow(product);

        assertThatThrownBy(() -> orderBusinessService.payForOrder())
                .isInstanceOf(ProductNotAvailableException.class);

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).save(any());
    }
}
