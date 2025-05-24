package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.product.service.ProductServiceFacade;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoOrderService implements OrderService {

    private final OrderRepository orderRepository;

    private final AuthenticationUserService authenticationUserService;

    private final ProductServiceFacade productServiceFacade;

    private final UserService userService;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        List<String> productIds = request.getProductIds();
        productIds.forEach(productServiceFacade::findProductOrThrow);

        return orderRepository.save(Order.builder()
                .ownerId(authenticatedUser.getId())
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
    public Order findByOwnerIdOrCreate() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerId(authenticatedUser.getId())
                .orElse(Order.builder()
                        .ownerId(authenticatedUser.getId())
                        .productIds(new ArrayList<>())
                        .status(OrderStatus.CREATED)
                        .build());
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
        Order order = validateOrderAccessOrThrow(orderId);
        orderRepository.delete(order);
    }

    @Override
    public Order addProductToOrder(String productId) {
        productServiceFacade.findProductOrThrow(productId);
        Order order = findByOwnerIdOrCreate();

        order.getProductIds().add(productId);
        order.setStatus(OrderStatus.IN_PROGRESS);

        return orderRepository.save(order);
    }

    private Order validateOrderAccessOrThrow(String orderId) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Order order = findOrderOrThrow(orderId);

        if (userService.validateEntityOwnerOrAdmin(authenticatedUser, order.getOwnerId())) {
            return order;
        }

        log.error("[ORDER_SERVICE_IMPL]: User {} is not owner of the order: {} or not ADMIN", authenticatedUser.getId(), orderId);
        throw new AccessDeniedException("Access denied!");
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[ORDER_SERVICE_IMPL]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }
}
