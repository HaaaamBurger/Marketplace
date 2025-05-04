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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

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


}
