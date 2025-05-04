package com.marketplace.order.service.impl;

import com.marketplace.auth.exception.EntityNotFoundException;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserRole;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final AuthHelper authHelper;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authHelper.getAuthenticatedUser();

        return orderRepository.save(Order.builder()
                .userId(authenticatedUser.getId())
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build());
    }

    @Override
    public Order findById(String orderId) {
        return validateOrderAccessOrThrow(orderId);
    }

    @Override
    public Order update(String orderId, OrderRequest request) {
        Order order = findOrderOrThrow(orderId);

        Optional.ofNullable(request.getProductIds()).ifPresent(order::setProductIds);
        Optional.ofNullable(request.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(request.getStatus()).ifPresent(order::setStatus);

        return orderRepository.save(order);
    }

    @Override
    public void delete(String orderId) {
        orderRepository.delete(findOrderOrThrow(orderId));
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT_SERVICE_IMPL]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }

    private Order validateOrderAccessOrThrow(String productId) {
        User authenticatedUser = authHelper.getAuthenticatedUser();
        Order product = findOrderOrThrow(productId);

        if (checkUserOwnerOrAdmin(authenticatedUser, product.getUserId())) {
            return product;
        }

        log.error("[PRODUCT_SERVICE_IMPL]: User {} is not owner of the order: {} or not ADMIN", authenticatedUser.getId(), productId);
        throw new AccessDeniedException("Access denied!");
    }

    private boolean checkUserOwnerOrAdmin(User user, String productUserId) {
        return Objects.equals(user.getId(), productUserId) || user.getRole() == UserRole.ADMIN;
    }

}
