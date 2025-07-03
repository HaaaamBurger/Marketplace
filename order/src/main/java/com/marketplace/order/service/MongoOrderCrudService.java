package com.marketplace.order.service;

import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.dto.OrderRequest;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoOrderCrudService implements OrderCrudService {

    private final OrderRepository orderRepository;

    private final AuthenticationUserService authenticationUserService;

    private final ProductCrudService productCrudService;

    private final OrderManagerService orderManagerService;

    private final OrderValidationService orderValidationService;

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();

        List<Product> products = request.getProductIds().stream().map(productCrudService::getById).toList();

        return orderRepository.save(Order.builder()
                .ownerId(authenticatedUser.getId())
                .products(new HashSet<>(products))
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
        Order order = orderManagerService.findOrderOrThrow(orderId);
        orderValidationService.validateOrderAccessOrThrow(order);

        return order;
    }

    @Override
    public Order update(String orderId, OrderUpdateRequest request) {
        Order order = orderManagerService.findOrderOrThrow(orderId);
        orderValidationService.validateOrderUpdateOrThrow(order);

        Optional.ofNullable(request.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(request.getStatus()).ifPresent(order::setStatus);

        return orderRepository.save(order);
    }

    @Override
    public void delete(String orderId) {
        Order order = orderManagerService.findOrderOrThrow(orderId);
        orderValidationService.validateOrderAccessOrThrow(order);

        orderRepository.delete(order);
    }
}
