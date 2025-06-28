package com.marketplace.order.service;

import com.marketplace.order.config.OrderApplicationConfig;
import com.marketplace.order.exception.OrderUpdateException;
import com.marketplace.order.util.MockHelper;
import com.marketplace.order.util.builder.OrderDataBuilder;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.DefaultUserValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = OrderApplicationConfig.class)
public class DefaultOrderValidationServiceTest {

    @MockitoBean
    private AuthenticationUserService authenticationUserService;

    @MockitoBean
    private DefaultUserValidationService defaultUserValidationService;

    @Autowired
    private DefaultOrderValidationService defaultOrderValidationService;

    @Autowired
    private MockHelper mockHelper;

    @Test
    public void validateOrderUpdateOrThrow_ShouldValidateOrderSuccessfully() {
        Order mockedOrder = mock(Order.class);
        defaultOrderValidationService.validateOrderUpdateOrThrow(mockedOrder);
    }

    @Test
    public void validateOrderUpdateOrThrow_ShouldThrowException_WhenOrderCompleted() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .build();

        assertThatThrownBy(() -> defaultOrderValidationService.validateOrderUpdateOrThrow(order)).isInstanceOf(OrderUpdateException.class);
    }

    @Test
    public void validateOrderUpdateOrThrow_ShouldThrowException_WhenOrderCancelled() {
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.CANCELLED)
                .build();

        assertThatThrownBy(() -> defaultOrderValidationService.validateOrderUpdateOrThrow(order)).isInstanceOf(OrderUpdateException.class);
    }

    @Test
    public void validateOrderAccessOrThrow_ShouldValidateOrderSuccessfully() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order mockedOrder = mock(Order.class);

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, mockedOrder.getOwnerId())).thenReturn(true);

        defaultOrderValidationService.validateOrderAccessOrThrow(mockedOrder);
    }

    @Test
    public void validateOrderAccessOrThrow_ShouldThrowException_WhenNoSecurity() {
        Order mockedOrder = mock(Order.class);

        when(authenticationUserService.getAuthenticatedUser()).thenThrow(AuthenticationCredentialsNotFoundException.class);

        assertThatThrownBy(() -> defaultOrderValidationService.validateOrderAccessOrThrow(mockedOrder)).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    public void validateOrderAccessOrThrow_ShouldThrowException_WhenNotOwner() {
        User user = mockHelper.mockAuthenticationAndSetContext();
        Order mockedOrder = mock(Order.class);

        when(authenticationUserService.getAuthenticatedUser()).thenReturn(user);
        when(defaultUserValidationService.validateEntityOwnerOrAdmin(user, mockedOrder.getOwnerId())).thenReturn(false);

        assertThatThrownBy(() -> defaultOrderValidationService.validateOrderAccessOrThrow(mockedOrder)).isInstanceOf(AccessDeniedException.class);
    }
}
