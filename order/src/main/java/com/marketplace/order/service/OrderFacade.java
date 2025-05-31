package com.marketplace.order.service;

import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.product.exception.ProductAmountNotEnoughException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade implements OrderCrudService, OrderSettingsService {

    private final OrderRepository orderRepository;

    private final AuthenticationUserService authenticationUserService;

    private final ProductCrudService productCrudService;

    private final UserSettingsService userSettingsService;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        List<String> productIds = request.getProductIds();
        productIds.forEach(productCrudService::getById);

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
    public Optional<Order> findByOwnerId() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerId(authenticatedUser.getId());
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
    public Order update(String orderId, OrderUpdateRequest request) {
        Order order = findOrderOrThrow(orderId);

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
    public void removeProductFromOrder(String productId) {
        Optional<Order> orderOptional = findByOwnerId();

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            List<String> filteredProducts = order.getProductIds().stream().filter(s -> !s.equals(productId)).toList();

            if (filteredProducts.isEmpty()) {
                orderRepository.deleteById(order.getId());
                return;
            }

            order.setProductIds(filteredProducts);
            orderRepository.save(order);
        }
    }

    @Transactional
    @Override
    public Order addProductToOrder(String productId) {
        // TODO the same logic were done in validation so it has been duplicated (think about the way how to avoid duplication)...not only here...
        Product product = productCrudService.getById(productId);
        validateEnoughAmountOrThrow(product.getAmount());

        Order order = findByOwnerIdOrCreate();

        order.getProductIds().add(productId);
        order.setStatus(OrderStatus.IN_PROGRESS);

        return orderRepository.save(order);
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

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("[MONGO_ORDER_SERVICE]: Order not found by ID {}", orderId);
                    return new EntityNotFoundException("Order not found!");
                });
    }

    private void validateEnoughAmountOrThrow(int amount) {
        if (amount == 0) {
            throw new ProductAmountNotEnoughException("There are no products available");
        }
    }
}
