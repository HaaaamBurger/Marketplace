package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.exception.OrderUpdateException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.util.MockHelper;

import com.marketplace.order.util.OrderDataBuilder;
import com.marketplace.order.util.ProductDataBuilder;
import com.marketplace.order.util.UserDataBuilder;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
class OrderFacadeTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private OrderCrudService orderCrudService;

    @Autowired
    private OrderSettingsService orderSettingsService;

    @Autowired
    private MockHelper mockHelper;

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void create_ShouldCreateProduct() {
        String mockProductId = "mockProductId";
        Product mockedProduct = mock(Product.class);
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        User user = mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.of(mockedProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Order responseOrder = orderCrudService.create(orderRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds().stream().anyMatch(productId -> productId.equals(mockProductId))).isTrue();

        verify(productRepository, times(1)).findById(mockProductId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void create_ShouldThrowException_WhenNoSecurity() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderCrudService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void create_ShouldThrowException_WhenProductNotExists() {
        String mockProductId = "mockProductId";
        OrderRequest orderRequest = OrderRequest.builder()
                .productIds(Set.of(mockProductId))
                .build();

        mockHelper.mockAuthenticationAndSetContext();

        when(productRepository.findById(mockProductId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderCrudService.create(orderRequest));
        assertThat(exception.getMessage()).isEqualTo("Product not found!");

        verify(productRepository, times(1)).findById(mockProductId);
    }

    @Test
    public void findAll_ShouldReturnAllOrders() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> orders = orderCrudService.findAll();

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

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order responseOrder = orderCrudService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void findById_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCrudService.findById(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository).findById(orderId);
    }

    @Test
    public void findById_ShouldThrowException_WhenUserNotOwner() {
        mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderCrudService.findById(order.getId()))
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

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderCrudService.findById(order.getId()));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void findById_ShouldReturnOrder_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order responseOrder = orderCrudService.findById(order.getId());
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());

        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void update_ShouldUpdateOrder() {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderCrudService.update(order.getId(), orderUpdateRequest);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());
        assertThat(responseOrder.getStatus()).isEqualTo(orderUpdateRequest.getStatus());

        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void update_ShouldThrowException_WhenOrderNotFound() {
        String orderId = String.valueOf(UUID.randomUUID());
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .status(OrderStatus.IN_PROGRESS)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderCrudService.update(orderId, orderUpdateRequest));
        assertThat(exception.getMessage()).isEqualTo("Order not found!");

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    public void update_ShouldThrowException_WhenOrderHasStatusCompleted() {
        String orderId = String.valueOf(UUID.randomUUID());
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .build();
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder().build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderUpdateException exception = assertThrows(OrderUpdateException.class, () ->
                orderCrudService.update(orderId, orderUpdateRequest)
        );
        assertThat(exception.getMessage()).isEqualTo("Completed order cannot be updated");

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    public void delete_ShouldDeleteOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        orderCrudService.delete(order.getId());

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    public void delete_ShouldThrowException_WhenOrderNotFound() {
        mockHelper.mockAuthenticationAndSetContext();
        String orderId = String.valueOf(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCrudService.delete(orderId))
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

        AuthenticationCredentialsNotFoundException exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderCrudService.delete(order.getId()));
        assertThat(exception.getMessage()).isEqualTo("Authentication is unavailable!");
    }

    @Test
    public void delete_ShouldReturnOrder_WhenUserNotOwnerButAdmin() {
        User user = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        mockHelper.mockAuthenticationAndSetContext(user);
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderCrudService.delete(order.getId());

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

        Order responseOrder = orderSettingsService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds()).isNotNull();
        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
        assertThat(responseOrder.getProductIds().stream().anyMatch(productId -> productId.equals(product.getId()))).isTrue();
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldCreateOrderAndAddProductToOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findOrderByOwnerId(user.getId())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order responseOrder = orderSettingsService.addProductToOrder(product.getId());

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOwnerId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds()).isNotNull();
        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
        assertThat(responseOrder.getProductIds().stream().anyMatch(productId -> productId.equals(product.getId()))).isTrue();
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void addProductToOrder_ShouldThrowException_WhenProductNotExists() {
        mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderSettingsService.addProductToOrder(product.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found!");

        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    public void findOrderByOwnerIdAndStatus_ShouldReturnOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED)).thenReturn(Optional.of(order));
        Optional<Order> orderByOwnerIdAndStatus = orderSettingsService.findOrderByOwnerIdAndStatus(OrderStatus.CREATED);
        assertThat(orderByOwnerIdAndStatus).isPresent();
        assertThat(orderByOwnerIdAndStatus.get().getOwnerId()).isEqualTo(user.getId());

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED);
    }

    @Test
    public void findOrderByOwnerIdAndStatus_ShouldReturnEmptyOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.CREATED)).thenReturn(Optional.of(order));
        Optional<Order> orderByOwnerIdAndStatus = orderSettingsService.findOrderByOwnerIdAndStatus(OrderStatus.IN_PROGRESS);
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
        List<Order> ordersByOwnerIdAndStatusIn = orderSettingsService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));

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
        List<Order> ordersByOwnerIdAndStatusIn = orderSettingsService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED));

        assertThat(ordersByOwnerIdAndStatusIn).isNotNull();
        assertThat(ordersByOwnerIdAndStatusIn.size()).isEqualTo(0);

        verify(orderRepository, times(1)).findOrdersByOwnerIdAndStatusIn(user.getId(), List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED));
    }

    @Test
    public void removeProductFromOrder_ShouldRemoveProductFromOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .productIds(new HashSet<>(Set.of(product.getId(), product1.getId())))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        orderSettingsService.removeProductFromOrder(product.getId());

        assertThat(order.getProductIds()).isNotNull();
        assertThat(order.getProductIds().size()).isEqualTo(1);
        assertThat(order.getProductIds().contains(product1.getId())).isTrue();

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void removeProductFromOrder_ShouldRemoveProductFromOrderAndOrder() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(user.getId())
                .status(OrderStatus.IN_PROGRESS)
                .productIds(new HashSet<>(Set.of(product.getId())))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS))
                .thenReturn(Optional.of(order));

        orderSettingsService.removeProductFromOrder(product.getId());

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).deleteById(order.getId());
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
                .productIds(Set.of(product.getId()))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        orderSettingsService.removeProductFromOrder(product1);

        assertThat(order.getProductIds()).isNotNull();
        assertThat(order.getProductIds().size()).isEqualTo(1);
        assertThat(order.getProductIds().contains(product.getId())).isTrue();

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).deleteById(order.getId());
        verify(orderRepository, never()).save(order);
    }

    @Test
    public void removeProductFromOrder_ShouldThrowException_WhenOrderNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderSettingsService.removeProductFromOrder(product.getId()))
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
                .productIds(Set.of(product.getId()))
                .build();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(order));
        orderSettingsService.payForOrder();

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void payForOrder_ShouldThrowException_WhenOrderNotFound() {
        User user = mockHelper.mockAuthenticationAndSetContext();

        when(orderRepository.findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderSettingsService.payForOrder())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found!");

        verify(orderRepository, times(1)).findOrderByOwnerIdAndStatus(user.getId(), OrderStatus.IN_PROGRESS);
        verify(orderRepository, never()).save(any());
    }

}
