package com.marketplace.order.service;

import com.marketplace.order.exception.OrderUpdateException;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOrderValidationService implements OrderValidationService {

    private final AuthenticationUserService authenticationUserService;

    private final UserValidationService userValidationService;

    @Override
    public void validateOrderUpdateOrThrow(Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("[ORDER_FACADE]: Order with statuses: COMPLETED or CANCELLED cannot be updated!");
            throw new OrderUpdateException("Completed order cannot be updated");
        }
    }

    @Override
    public void validateOrderAccessOrThrow(Order order) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        if (userValidationService.validateEntityOwnerOrAdmin(authenticatedUser, order.getOwnerId())) {
            return;
        }

        log.error("[MONGO_ORDER_SERVICE]: User {} is not owner of the order: {} or not ADMIN", authenticatedUser.getId(), order.getId());
        throw new AccessDeniedException("Access denied!");
    }
}
