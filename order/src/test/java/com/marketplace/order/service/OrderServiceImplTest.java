package com.marketplace.order.service;


import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.web.model.User;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.service.impl.OrderServiceImpl;
import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.web.model.Order;

import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldReturnAllOrders() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
    }

    @Test
    void shouldReturnOrderById() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(order.getId());

        assertEquals(order.getId(), result.getId());
    }

    @Test
    void shouldThrowIfOrderNotFound() {
        String id = UUID.randomUUID().toString();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(id));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void shouldCreateOrder() {
        String userId = "test-user-id";
        String email = "user@example.com";


        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);


        when(userRepository.findByEmail(email)).thenReturn(Optional.of(
                User.builder().id(userId).email(email).build()
        ));


        OrderRequest request = new OrderRequest(List.of("product1", "product2"), "Some Address", OrderStatus.CREATED);

        Order savedOrder = Order.builder()
                .id("order-id-123")
                .userId(userId)
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);


        OrderResponse result = orderService.createOrder(request);

        assertEquals(savedOrder.getId(), result.getId());
        assertEquals(savedOrder.getUserId(), result.getUserId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldUpdateOrder() {
        Order existingOrder = OrderDataBuilder.buildOrderWithAllFields().build();
        OrderRequest request = new OrderRequest(List.of("newProduct"), "New Address",  OrderStatus.SHIPPED);

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder.toBuilder()
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build());

        OrderResponse updated = orderService.updateOrder(existingOrder.getId(), request);

        assertEquals("New Address", updated.getAddress());
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());

    }

    @Test
    void shouldDeleteOrder() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.deleteOrder(order.getId());

        verify(orderRepository).delete(order);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
