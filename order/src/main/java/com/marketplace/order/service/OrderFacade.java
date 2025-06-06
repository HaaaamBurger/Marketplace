package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.exception.OrderUpdateException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.product.exception.ProductNotAvailableException;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade implements OrderCrudService, OrderSettingsService {

    private final OrderRepository orderRepository;

    private final AuthenticationUserService authenticationUserService;

    private final ProductCrudService productCrudService;

    private final ProductRepository productRepository;

    private final UserSettingsService userSettingsService;

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        Set<String> productIds = request.getProductIds();
        productIds.forEach(productCrudService::getById);

        return orderRepository.save(Order.builder()
                .ownerId(authenticatedUser.getId())
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build());
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(String orderId) {
        return validateOrderAccessOrThrow(orderId);
    }

    @Override
    public Order update(String orderId, OrderUpdateRequest request) {
        Order order = findOrderOrThrow(orderId);

        validateOrderUpdateOrThrow(order);

        Optional.ofNullable(request.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(request.getStatus()).ifPresent(order::setStatus);

        return orderRepository.save(order);
    }

    @Override
    public void delete(String orderId) {
        Order order = validateOrderAccessOrThrow(orderId);
        orderRepository.delete(order);
    }

    @Transactional
    @Override
    public Order addProductToOrder(String productId) {
        // TODO the same logic were done in validation so it has been duplicated (think about the way how to avoid duplication)...not only here... better to avoid db calls in validators if possible
        Product product = productCrudService.getById(productId);
        validateEnoughAmountOrThrow(product.getAmount());

        Order order = findByOwnerIdOrCreate();
        order.getProductIds().add(productId);
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findOrderByOwnerIdAndStatus(OrderStatus orderStatus) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), orderStatus);
    }

    @Override
    public List<Order> findOrdersByOwnerIdAndStatusIn(List<OrderStatus> orderStatuses) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrdersByOwnerIdAndStatusIn(authenticatedUser.getId(), orderStatuses);
    }

    @Override
    public void removeProductFromOrder(String productId) {
        Order order = findOrderByOwnerIdAndStatusOrThrow(OrderStatus.IN_PROGRESS);
        Set<String> productIds = order.getProductIds();

        if (!productIds.contains(productId)) {
            return;
        }

        productIds.remove(productId);
        if (productIds.isEmpty()) {
            orderRepository.deleteById(order.getId());
            return;
        }

        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void payForOrder() {
        Order order = findActiveOrderByOwnerIdOrThrow();

        //TODO Add validation for product access
        order.getProductIds().stream().map(productCrudService::getById).forEach(product -> {

            boolean hasAmountDecreased = product.decreaseAmount();
            if (!hasAmountDecreased) {
                log.warn("[ORDER_FACADE]: Product {} amount is 0", product.getId());
                throw new ProductNotAvailableException("Currently product not available!");
            }

            productRepository.save(product);
        });

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    private Order validateOrderAccessOrThrow(String orderId) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Order order = findOrderOrThrow(orderId);

        if (userSettingsService.validateEntityOwnerOrAdmin(authenticatedUser, order.getOwnerId())) {
            return order;
        }

        log.error("[MONGO_ORDER_SERVICE]: User {} is not owner of the order: {} or not ADMIN", authenticatedUser.getId(), orderId);
        throw new AccessDeniedException("Access denied!");
    }

    private Order findByOwnerIdOrCreate() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), OrderStatus.IN_PROGRESS)
                .orElse(Order.builder()
                        .ownerId(authenticatedUser.getId())
                        .productIds(new HashSet<>())
                        .status(OrderStatus.IN_PROGRESS)
                        .build()
                );
    }

    private Order findActiveOrderByOwnerIdOrThrow() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), OrderStatus.IN_PROGRESS)
                .orElseThrow(() -> {
                    log.error("[MONGO_ORDER_SERVICE]: Order not found by ownerId {}", authenticatedUser.getId());
                    return new EntityNotFoundException("Order not found!");
                });
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[MONGO_ORDER_SERVICE]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }

    private void validateOrderUpdateOrThrow(Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("[ORDER_FACADE]: Order with statuses: COMPLETED or CANCELLED cannot be updated!");
            throw new OrderUpdateException("Completed order cannot be updated");
        }
    }

    private void validateEnoughAmountOrThrow(int amount) {
        if (amount == 0) {
            throw new ProductNotAvailableException("There are no products available");
        }
    }

    private Order findOrderByOwnerIdAndStatusOrThrow(OrderStatus orderStatus) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Optional<Order> orderByOwnerIdAndStatus = orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), orderStatus);

        if (orderByOwnerIdAndStatus.isEmpty()) {
            log.error("[ORDER_FACADE]: Order not found by ownerId {} and status {}", authenticatedUser.getId(), orderStatus);
            throw new EntityNotFoundException("Order not found!");
        }

        return orderByOwnerIdAndStatus.get();
    }
}
