package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.exception.OrderUpdateException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.MockHelper;
import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.util.UserDataBuilder;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.service.MongoProductCrudService;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.security.AuthenticationUserService;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
public class MongoOrderCrudServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private MongoProductCrudService mongoProductCrudService;

    @MockitoBean
    private OrderBusinessService orderBusinessService;

    @MockitoBean
    private DefaultOrderValidationService defaultOrderValidationService;

    @MockitoBean
    private AuthenticationUserService authenticationUserService;

    @Autowired
    private MongoOrderCrudService mongoOrderCrudService;

    @Autowired
    private MockHelper mockHelper;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void create_ShouldCreateOrder() {
        String mockProductId = "mockProductId";
        Product mockedProduct = mock(Product.class);
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        User user = mockHelper.mockAuthenticationAndSetContext();

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(mongoProductCrudService.getById(mockProductId)).thenReturn(mockedProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Order responseOrder = mongoOrderCrudService.create(orderRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds().stream().anyMatch(productId -> productId.equals(mockProductId))).isTrue();

        verify(authenticationUserService, times(1)).getAuthenticatedUser();
        verify(mongoProductCrudService, times(1)).getById(mockProductId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void create_ShouldThrowException_WhenNoSecurity() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        when(authenticationUserService.getAuthenticatedUser()).thenThrow(AuthenticationCredentialsNotFoundException.class);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> mongoOrderCrudService.create(orderRequest));
    }

    @Test
    public void create_ShouldThrowException_WhenProductNotExists() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        mockHelper.mockAuthenticationAndSetContext();

        when(mongoProductCrudService.getById(mockProductId)).thenThrow(new EntityNotFoundException("Product not found!"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> mongoOrderCrudService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");

        verify(mongoProductCrudService, times(1)).getById(mockProductId);
    }

    @Test
    public void findAll_ShouldReturnAllOrders() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> orders = mongoOrderCrudService.findAll();

        assertEquals(1, orders.size());
        assertEquals(orders.get(0).getId(), orders.get(0).getId());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void findById_ShouldReturnOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);

        Order responseOrder = mongoOrderCrudService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderBusinessService).findOrderOrThrow(order.getId());
    }

    @Test
    public void findById_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderBusinessService.findOrderOrThrow(orderId)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> mongoOrderCrudService.findById(orderId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orderBusinessService).findOrderOrThrow(orderId);
    }

    @Test
    public void findById_ShouldThrowException_WhenUserNotOwner() {
        mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenThrow(AccessDeniedException.class);

        assertThatThrownBy(() -> mongoOrderCrudService.findById(order.getId()))
                .isInstanceOf(AccessDeniedException.class);

        verify(orderBusinessService).findOrderOrThrow(order.getId());
    }

    @Test
    public void findById_ShouldThrowException_WhenNoSecurity() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(ownerId)
                .build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);
        doThrow(AuthenticationCredentialsNotFoundException.class).when(defaultOrderValidationService).validateOrderAccessOrThrow(order);

        assertThatThrownBy(() -> mongoOrderCrudService.findById(order.getId()))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    public void findById_ShouldReturnOrder_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);

        Order responseOrder = mongoOrderCrudService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderBusinessService).findOrderOrThrow(order.getId());
    }

    @Test
    public void update_ShouldUpdateOrder() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = mongoOrderCrudService.update(order.getId(), orderUpdateRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());
        assertThat(responseOrder.getStatus()).isEqualTo(orderUpdateRequest.getStatus());

        verify(orderBusinessService, times(1)).findOrderOrThrow(order.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void update_ShouldThrowException_WhenOrderNotFound() {
        String orderId = String.valueOf(UUID.randomUUID());
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderBusinessService.findOrderOrThrow(orderId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> mongoOrderCrudService.update(orderId, orderUpdateRequest));

        verify(orderBusinessService, times(1)).findOrderOrThrow(orderId);
    }

    @Test
    public void update_ShouldThrowException_WhenOrderHasStatusCompleted() {
        String orderId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .build();
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder().build();

        when(orderBusinessService.findOrderOrThrow(orderId)).thenReturn(order);
        doThrow(OrderUpdateException.class).when(defaultOrderValidationService).validateOrderUpdateOrThrow(order);

        assertThrows(OrderUpdateException.class, () ->
                mongoOrderCrudService.update(orderId, orderUpdateRequest)
        );

        verify(orderBusinessService, times(1)).findOrderOrThrow(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    public void delete_ShouldDeleteOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);
        mongoOrderCrudService.delete(order.getId());

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    public void delete_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderBusinessService.findOrderOrThrow(orderId)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> mongoOrderCrudService.delete(orderId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orderBusinessService).findOrderOrThrow(orderId);
    }

    @Test
    public void delete_ShouldThrowException_WhenNoSecurity() {
        String ownerId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(ownerId)
                .build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);
        doThrow(AuthenticationCredentialsNotFoundException.class).when(defaultOrderValidationService).validateOrderAccessOrThrow(order);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> mongoOrderCrudService.delete(order.getId()));
    }

    @Test
    public void delete_ShouldReturnOrder_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderBusinessService.findOrderOrThrow(order.getId())).thenReturn(order);

        mongoOrderCrudService.delete(order.getId());

        verify(orderRepository, times(1)).delete(order);
    }

}
