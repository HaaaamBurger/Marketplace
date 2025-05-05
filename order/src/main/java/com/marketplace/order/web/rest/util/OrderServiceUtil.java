package com.marketplace.order.web.rest.util;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceUtil {

    private final OrderRepository orderRepository;

    private final AuthHelper authHelper;

    public Order validateOrderAccessOrThrow(String orderId) {
        User authenticatedUser = authHelper.getAuthenticatedUser();
        Order order = findOrderOrThrow(orderId);

        if (checkUserOwnerOrAdmin(authenticatedUser, order.getUserId())) {
            return order;
        }

        log.error("[ORDER_SERVICE_IMPL]: User {} is not owner of the order: {} or not ADMIN", authenticatedUser.getId(), orderId);
        throw new AccessDeniedException("Access denied!");
    }

    public Order findOrderOrCreate() {
        User authenticatedUser = authHelper.getAuthenticatedUser();
        return orderRepository.findOrderByUserId(authenticatedUser.getId())
                .orElse(Order.builder()
                        .userId(authenticatedUser.getId())
                        .productIds(new ArrayList<>())
                        .status(OrderStatus.CREATED)
                        .build());
    }

    public Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[ORDER_SERVICE_IMPL]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }

    private boolean checkUserOwnerOrAdmin(User user, String productUserId) {
        return Objects.equals(user.getId(), productUserId) || user.getRole() == UserRole.ADMIN;
    }

}
