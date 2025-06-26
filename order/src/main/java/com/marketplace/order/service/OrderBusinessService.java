package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.service.ProductBusinessService;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.service.ProductValidationService;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderBusinessService implements OrderManagerService {

    private final AuthenticationUserService authenticationUserService;

    private final OrderRepository orderRepository;

    private final ProductCrudService productCrudService;

    private final ProductBusinessService productBusinessService;

    private final ProductValidationService productValidationService;

    @Transactional
    @Override
    public Order addProductToOrder(String productId) {
        Product product = productCrudService.getById(productId);
        productValidationService.validateProductOrThrow(product);

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

        List<Product> products = productBusinessService.findAllByIdIn(order.getProductIds());
        products.forEach(productValidationService::validateProductOrThrow);

        productBusinessService.decreaseProductsAmountAndSave(products);

        order.setTotal(calculateTotalSum(products));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    public Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[MONGO_ORDER_SERVICE]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }

    @Override
    public BigDecimal calculateTotalSum(List<Product> products) {
        return products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
