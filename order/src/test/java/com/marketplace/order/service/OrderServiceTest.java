package com.marketplace.order.service;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.MockHelper;

import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.util.UserDataBuilder;
import com.marketplace.order.web.model.Order;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
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
    public void findById_shouldReturnOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .userId(user.getId())
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order responseOrder = orderService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_shouldThrowException() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository).findById(orderId);
    }

    @Test
    public void findById_shouldThrowException_WhenUserNotMatching() {
        mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.findById(order.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied!");

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_shouldThrowException_WhenNoSecurity() {
        String userId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .userId(userId)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.findById(order.getId()));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void findById_shouldReturnOrder_WhenUserNotMatchingButAdmin() {
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
    public void create_shouldCreateProduct() {
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
        assertThat(responseOrder.getUserId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(mockProductId);
    }

    @Test
    public void create_shouldThrowException_WhenNoSecurity() {
        String mockProductId = "mockProductId";
        Product mockedProduct = mock(Product.class);
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(List.of(mockProductId))
                .build();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.of(mockedProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void create_shouldThrowException_WhenProductNotExists() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(List.of(mockProductId))
                .build();

        mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");

    }

}
