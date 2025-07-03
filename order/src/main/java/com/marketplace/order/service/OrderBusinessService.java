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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
        order.getProducts().add(product);

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

        boolean noneMatchProductById = order.getProducts().stream().noneMatch(product -> product.getId().equals(productId));
        if (noneMatchProductById) {
            return;
        }

        order.setProducts(order.getProducts().stream()
                .filter(product -> !product.getId().equals(productId))
                .collect(Collectors.toSet()));

        if (order.getProducts().isEmpty()) {
            orderRepository.deleteById(order.getId());
            return;
        }

        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void removeProductFromAllOrders(Product product) {
        List<Order> orders = orderRepository.findByProductsContainingAndStatusIn(Set.of(product), List.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS));
        removeProductAndDeleteEmptyOrders(orders, product.getId());
        orderRepository.saveAll(orders);
    }

    @Transactional
    @Override
    public void payForOrder() {
        Order order = findActiveOrderByOwnerIdOrThrow();

        Set<Product> products = order.getProducts();
        products.forEach(productValidationService::validateProductOrThrow);

        productBusinessService.decreaseProductsAmountAndSave(products);

        order.setTotal(calculateTotalSum(products));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    public Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found!"));
    }

    @Override
    public BigDecimal calculateTotalSum(Set<Product> products) {
        return products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void removeProductAndDeleteEmptyOrders(List<Order> orders, String productId) {
        Product product = productCrudService.getById(productId);

        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            boolean hasProductRemoved = order.getProducts().remove(product);

            if (hasProductRemoved && order.getProducts().isEmpty()) {
                orderRepository.delete(order);
                iterator.remove();
            }
        }
    }

    private Order findByOwnerIdOrCreate() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        return orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), OrderStatus.IN_PROGRESS)
                .orElse(Order.builder()
                        .ownerId(authenticatedUser.getId())
                        .products(new HashSet<>())
                        .status(OrderStatus.IN_PROGRESS)
                        .build()
                );
    }

    private Order findActiveOrderByOwnerIdOrThrow() {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        return orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), OrderStatus.IN_PROGRESS)
                .orElseThrow(() -> new EntityNotFoundException("Order not found!"));
    }

    private Order findOrderByOwnerIdAndStatusOrThrow(OrderStatus orderStatus) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        Optional<Order> orderByOwnerIdAndStatus = orderRepository.findOrderByOwnerIdAndStatus(authenticatedUser.getId(), orderStatus);

        if (orderByOwnerIdAndStatus.isEmpty()) {
            throw new EntityNotFoundException("Order not found!");
        }

        return orderByOwnerIdAndStatus.get();
    }

}
