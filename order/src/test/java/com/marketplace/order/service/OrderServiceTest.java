package com.marketplace.order.service;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.MockHelper;

import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.util.ProductDataBuilder;
import com.marketplace.order.util.UserDataBuilder;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
class OrderServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockHelper mockHelper;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void findById_ShouldReturnOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order responseOrder = orderService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository).findById(orderId);
    }

    @Test
    public void findById_ShouldThrowException_WhenUserNotMatching() {
        mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.findById(order.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied!");

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_ShouldThrowException_WhenNoSecurity() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(ownerId)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.findById(order.getId()));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void findById_ShouldReturnOrder_WhenUserNotMatchingButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order responseOrder = orderService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void create_ShouldCreateProduct() {
        String mockProductId = "mockProductId";
        Product mockedProduct = mock(Product.class);
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(List.of(mockProductId))
                .build();

        User user = mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.of(mockedProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Order responseOrder = orderService.create(orderRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(mockProductId);

        verify(productRepository, times(1)).findById(mockProductId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void create_ShouldThrowException_WhenNoSecurity() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(List.of(mockProductId))
                .build();

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void create_ShouldThrowException_WhenProductNotExists() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(List.of(mockProductId))
                .build();

        mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");

        verify(productRepository, times(1)).findById(mockProductId);
    }

    @Test
    public void findAll_ShouldReturnAllOrders() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> orders = orderService.findAll();

        assertEquals(1, orders.size());
        assertEquals(orders.get(0).getId(), orders.get(0).getId());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void update_ShouldUpdateOrder() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        OrderRequest orderRequest = OrderRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderService.update(order.getId(), orderRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());
        assertThat(responseOrder.getStatus()).isEqualTo(orderRequest.getStatus());

        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void update_ShouldThrowException_WhenOrderNotFound() {
        String orderId = String.valueOf(UUID.randomUUID());
        OrderRequest orderRequest = OrderRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.update(orderId, orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Order not found!");

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    public void delete_ShouldDeleteOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        orderService.delete(order.getId());

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    public void delete_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository).findById(orderId);
    }

    @Test
    public void delete_ShouldThrowException_WhenNoSecurity() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(ownerId)
                .build();

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.delete(order.getId()));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void delete_ShouldReturnOrder_WhenUserNotMatchingButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.delete(order.getId());

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    public void addProductToOrder_ShouldAddProductToExistingOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findOrderByOwnerId(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds()).isNotNull();
        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(product.getId());
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerId(user.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldCreateOrderAndAddProductToOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findOrderByOwnerId(user.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds()).isNotNull();
        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(product.getId());
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerId(user.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldThrowException_WhenProductNotExists() {
        mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addProductToOrder(product.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found!");

        verify(productRepository, times(1)).findById(product.getId());
    }

}
